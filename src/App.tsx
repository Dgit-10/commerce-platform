import React, { useState, useEffect, useId } from 'react';
import {
  Shield,
  Layers,
  Server,
  Activity,
  CreditCard,
  Bell,
  Package,
  Users,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  Terminal,
  Play,
  RotateCcw,
  Key,
  Database,
  Radio,
  FileCode2,
  Clock,
  Send,
  Zap,
  Check,
  XCircle,
  Copy,
  Info
} from 'lucide-react';

interface LogEntry {
  id: string;
  timestamp: string;
  service: string;
  level: 'INFO' | 'WARN' | 'ERROR';
  correlationId: string;
  message: string;
  metadata?: Record<string, any>;
}

interface KafkaEvent {
  eventId: string;
  topic: string;
  correlationId: string;
  timestamp: string;
  payload: any;
  status: 'PRODUCED' | 'CONSUMED' | 'RETRYING' | 'SENT_TO_DLQ';
  attempts: number;
}

interface OrderRecord {
  id: number;
  userId: number;
  userName: string;
  totalAmount: number;
  items: Array<{ productId: number; name: string; quantity: number; price: number }>;
  status: 'AWAITING_APPROVAL' | 'PAID' | 'CANCELLED';
  correlationId: string;
  createdAt: string;
}

interface PaymentRecord {
  id: number;
  orderId: number;
  userId: number;
  amount: number;
  status: 'PENDING_APPROVAL' | 'APPROVED' | 'DECLINED';
  transactionId?: string;
  correlationId: string;
  createdAt: string;
}

interface NotificationRecord {
  id: number;
  userId: number;
  type: string;
  message: string;
  correlationId: string;
  createdAt: string;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'tracing' | 'kafka' | 'code'>('simulator');
  
  // Auth state
  const [authToken, setAuthToken] = useState<string>('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MiIsImVtYWlsIjoiYWxpY2VAZXhhbXBsZS5jb20iLCJyb2xlIjoiUk9MRV9VU0VSIiwiZXhwIjoxNzg3NzYwODAwfQ.signature');
  const [currentUser, setCurrentUser] = useState({ id: 42, email: 'alice@example.com', name: 'Alice Smith', role: 'ROLE_USER' });
  
  // Microservice State Stores
  const [orders, setOrders] = useState<OrderRecord[]>([
    {
      id: 101,
      userId: 42,
      userName: 'Alice Smith',
      totalAmount: 189.98,
      items: [
        { productId: 1, name: 'Ergonomic Mechanical Keyboard', quantity: 1, price: 129.99 },
        { productId: 2, name: 'Precision Wireless Mouse', quantity: 1, price: 59.99 }
      ],
      status: 'AWAITING_APPROVAL',
      correlationId: 'c-9821-init',
      createdAt: '2026-08-25 23:30:12'
    }
  ]);

  const [payments, setPayments] = useState<PaymentRecord[]>([
    {
      id: 501,
      orderId: 101,
      userId: 42,
      amount: 189.98,
      status: 'PENDING_APPROVAL',
      correlationId: 'c-9821-init',
      createdAt: '2026-08-25 23:30:13'
    }
  ]);

  const [notifications, setNotifications] = useState<NotificationRecord[]>([]);

  const [logs, setLogs] = useState<LogEntry[]>([
    {
      id: 'log-1',
      timestamp: '23:30:11.890',
      service: 'API-GATEWAY',
      level: 'INFO',
      correlationId: 'c-9821-init',
      message: 'GATEWAY_HTTP_ACCESS | method=POST path=/api/v1/orders status=201 latencyMs=42 clientIp=192.168.1.5'
    },
    {
      id: 'log-2',
      timestamp: '23:30:12.010',
      service: 'ORDER-SERVICE',
      level: 'INFO',
      correlationId: 'c-9821-init',
      message: 'Created Order #101 with initial status AWAITING_APPROVAL for userId=42. Emitting OrderCreatedEvent to Kafka.'
    },
    {
      id: 'log-3',
      timestamp: '23:30:13.150',
      service: 'PAYMENT-SERVICE',
      level: 'INFO',
      correlationId: 'c-9821-init',
      message: 'PaymentEventConsumer: Initialized Payment #501 with status PENDING_APPROVAL. Awaiting explicit approval.'
    }
  ]);

  const [kafkaEvents, setKafkaEvents] = useState<KafkaEvent[]>([
    {
      eventId: 'evt-ord-101',
      topic: 'order-created-topic',
      correlationId: 'c-9821-init',
      timestamp: '23:30:12.500',
      payload: { orderId: 101, userId: 42, totalAmount: 189.98 },
      status: 'CONSUMED',
      attempts: 1
    }
  ]);

  const [selectedCorrelationId, setSelectedCorrelationId] = useState<string>('all');
  const [selectedCodeSection, setSelectedCodeSection] = useState<'gateway' | 'payment' | 'correlation' | 'kafka-resilience' | 'docker'>('gateway');
  const [isSubmittingOrder, setIsSubmittingOrder] = useState(false);
  const [isApproving, setIsApproving] = useState<number | null>(null);
  const [copiedToken, setCopiedToken] = useState(false);

  // New order form inputs
  const [selectedProduct, setSelectedProduct] = useState({ id: 3, name: 'Noise-Cancelling Studio Headphones', price: 249.50 });
  const [itemQty, setItemQty] = useState(1);

  const addLog = (service: string, level: 'INFO' | 'WARN' | 'ERROR', correlationId: string, message: string, metadata?: any) => {
    const time = new Date().toTimeString().split(' ')[0] + '.' + String(new Date().getMilliseconds()).padStart(3, '0');
    const newEntry: LogEntry = {
      id: 'log-' + Math.random().toString(36).substring(2, 9),
      timestamp: time,
      service,
      level,
      correlationId,
      message,
      metadata
    };
    setLogs(prev => [newEntry, ...prev.slice(0, 99)]);
  };

  const handleCreateOrder = () => {
    setIsSubmittingOrder(true);
    const corrId = 'c-' + Math.floor(1000 + Math.random() * 9000);
    const orderId = orders.length + 101;
    const paymentId = payments.length + 501;
    const totalAmount = selectedProduct.price * itemQty;
    const now = new Date().toISOString().replace('T', ' ').substring(0, 19);

    // Step 1: API Gateway receives request
    addLog('API-GATEWAY', 'INFO', corrId, `GATEWAY_HTTP_ACCESS | method=POST path=/api/v1/orders status=201 clientIp=127.0.0.1 tokenSubject=${currentUser.id}`);

    setTimeout(() => {
      // Step 2: Order Service processes & sets AWAITING_APPROVAL
      const newOrder: OrderRecord = {
        id: orderId,
        userId: currentUser.id,
        userName: currentUser.name,
        totalAmount,
        items: [{ productId: selectedProduct.id, name: selectedProduct.name, quantity: itemQty, price: selectedProduct.price }],
        status: 'AWAITING_APPROVAL',
        correlationId: corrId,
        createdAt: now
      };
      setOrders(prev => [newOrder, ...prev]);
      addLog('ORDER-SERVICE', 'INFO', corrId, `Order #${orderId} stored with status AWAITING_APPROVAL. Publishing to order-created-topic.`);

      // Step 3: Kafka Event Published
      const eventId = 'evt-ord-' + orderId;
      const kafkaEvt: KafkaEvent = {
        eventId,
        topic: 'order-created-topic',
        correlationId: corrId,
        timestamp: new Date().toTimeString().split(' ')[0],
        payload: { orderId, userId: currentUser.id, totalAmount },
        status: 'CONSUMED',
        attempts: 1
      };
      setKafkaEvents(prev => [kafkaEvt, ...prev]);

      // Step 4: Payment Service Consumer
      setTimeout(() => {
        const newPayment: PaymentRecord = {
          id: paymentId,
          orderId,
          userId: currentUser.id,
          amount: totalAmount,
          status: 'PENDING_APPROVAL',
          correlationId: corrId,
          createdAt: now
        };
        setPayments(prev => [newPayment, ...prev]);
        addLog('PAYMENT-SERVICE', 'INFO', corrId, `Payment #${paymentId} created in PENDING_APPROVAL. (No notification sent until approval).`);
        setIsSubmittingOrder(false);
      }, 500);
    }, 400);
  };

  const handleApprovePayment = (paymentId: number) => {
    setIsApproving(paymentId);
    const payment = payments.find(p => p.id === paymentId);
    if (!payment) return;

    const corrId = 'c-appr-' + Math.floor(1000 + Math.random() * 9000);
    const txnId = 'TXN_' + Math.random().toString(36).substring(2, 8).toUpperCase();

    // 1. Gateway
    addLog('API-GATEWAY', 'INFO', corrId, `GATEWAY_HTTP_ACCESS | method=POST path=/api/v1/payments/${paymentId}/approve status=200 user=${currentUser.id}`);

    setTimeout(() => {
      // 2. Payment Service Transition to APPROVED
      setPayments(prev => prev.map(p => p.id === paymentId ? { ...p, status: 'APPROVED', transactionId: txnId } : p));
      addLog('PAYMENT-SERVICE', 'INFO', corrId, `Payment #${paymentId} status transitioned to APPROVED. TxnId: ${txnId}. Emitting PaymentApprovedEvent.`);

      // 3. Kafka Event Emitted
      const approvedEvtId = 'evt-appr-' + paymentId;
      const kafkaEvt: KafkaEvent = {
        eventId: approvedEvtId,
        topic: 'payment-approved-topic',
        correlationId: corrId,
        timestamp: new Date().toTimeString().split(' ')[0],
        payload: { paymentId, orderId: payment.orderId, userId: payment.userId, amount: payment.amount, transactionId: txnId },
        status: 'CONSUMED',
        attempts: 1
      };
      setKafkaEvents(prev => [kafkaEvt, ...prev]);

      // 4. Order Service Consumes -> Status PAID
      setTimeout(() => {
        setOrders(prev => prev.map(o => o.id === payment.orderId ? { ...o, status: 'PAID' } : o));
        addLog('ORDER-SERVICE', 'INFO', corrId, `Order #${payment.orderId} updated to status PAID based on PaymentApprovedEvent.`);
      }, 350);

      // 5. Notification Service Consumes -> Dispatches Customer Alert
      setTimeout(() => {
        const notifId = notifications.length + 1;
        const newNotif: NotificationRecord = {
          id: notifId,
          userId: payment.userId,
          type: 'PAYMENT_SUCCESS',
          message: `Payment of $${payment.amount.toFixed(2)} approved for Order #${payment.orderId}. Txn: ${txnId}`,
          correlationId: corrId,
          createdAt: new Date().toTimeString().split(' ')[0]
        };
        setNotifications(prev => [newNotif, ...prev]);
        addLog('NOTIFICATION-SERVICE', 'INFO', corrId, `Dispatched Payment Approved push & email notification to user #${payment.userId}.`);
        setIsApproving(null);
      }, 600);
    }, 450);
  };

  const handleSimulateDLQRetry = () => {
    const corrId = 'c-err-' + Math.floor(1000 + Math.random() * 9000);
    const failEvtId = 'evt-fail-' + Math.floor(Math.random() * 1000);

    addLog('PAYMENT-SERVICE', 'WARN', corrId, `Consumer Error on event ${failEvtId}: Downstream gateway timeout. Scheduling Exponential Backoff Attempt 1 (1000ms).`);

    const retryEvt: KafkaEvent = {
      eventId: failEvtId,
      topic: 'order-created-topic',
      correlationId: corrId,
      timestamp: new Date().toTimeString().split(' ')[0],
      payload: { error: 'SIMULATED_NETWORK_FAILURE', testId: failEvtId },
      status: 'RETRYING',
      attempts: 1
    };
    setKafkaEvents(prev => [retryEvt, ...prev]);

    setTimeout(() => {
      addLog('PAYMENT-SERVICE', 'WARN', corrId, `Attempt 2 failed for ${failEvtId}. Exponential Backoff Attempt 2 (2000ms).`);
      setKafkaEvents(prev => prev.map(e => e.eventId === failEvtId ? { ...e, attempts: 2 } : e));

      setTimeout(() => {
        addLog('PAYMENT-SERVICE', 'ERROR', corrId, `Attempt 3 exhausted for ${failEvtId}. DeadLetterPublishingRecoverer routing event to order-created-topic.DLT`);
        setKafkaEvents(prev => prev.map(e => e.eventId === failEvtId ? { ...e, status: 'SENT_TO_DLQ', attempts: 3, topic: 'order-created-topic.DLT' } : e));
      }, 1000);
    }, 1000);
  };

  const filteredLogs = selectedCorrelationId === 'all'
    ? logs
    : logs.filter(l => l.correlationId === selectedCorrelationId);

  const correlationIdsList = Array.from(new Set(logs.map(l => l.correlationId)));

  const handleCopyToken = () => {
    navigator.clipboard.writeText(authToken);
    setCopiedToken(true);
    setTimeout(() => setCopiedToken(false), 2000);
  };

  return (
    <div id="v2-platform-container" className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Header */}
      <header id="v2-header" className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3.5 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="h-9 w-9 rounded-lg bg-gradient-to-tr from-indigo-600 to-cyan-500 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <Layers className="h-5 w-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg font-bold text-white tracking-tight">E-Commerce Microservices</h1>
                <span className="px-2 py-0.5 text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full">
                  V2 Architecture
                </span>
              </div>
              <p className="text-xs text-slate-400">Gateway Routing • JWT Propagation • Payment State Machine • Kafka DLQ</p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="flex items-center p-1 bg-slate-800/80 rounded-lg border border-slate-700/60">
            <button
              id="tab-simulator"
              onClick={() => setActiveTab('simulator')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-md text-xs font-medium transition-all ${
                activeTab === 'simulator'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Play className="h-3.5 w-3.5" />
              Live Workflow Simulator
            </button>
            <button
              id="tab-tracing"
              onClick={() => setActiveTab('tracing')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-md text-xs font-medium transition-all ${
                activeTab === 'tracing'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Activity className="h-3.5 w-3.5" />
              Correlation ID Tracing
            </button>
            <button
              id="tab-kafka"
              onClick={() => setActiveTab('kafka')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-md text-xs font-medium transition-all ${
                activeTab === 'kafka'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Radio className="h-3.5 w-3.5" />
              Kafka & DLQ Resilience
            </button>
            <button
              id="tab-code"
              onClick={() => setActiveTab('code')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-md text-xs font-medium transition-all ${
                activeTab === 'code'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <FileCode2 className="h-3.5 w-3.5" />
              Code & Config Explorer
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 lg:p-8 space-y-6">
        
        {/* TOP STATUS BAR: Active Microservices Matrix */}
        <section id="services-health-bar" className="grid grid-cols-2 md:grid-cols-5 gap-3">
          <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Shield className="h-4 w-4 text-cyan-400" />
              <div>
                <div className="text-xs font-semibold text-slate-200">API Gateway</div>
                <div className="text-[11px] font-mono text-slate-400">:8080 • JWT Filter</div>
              </div>
            </div>
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          </div>

          <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Users className="h-4 w-4 text-indigo-400" />
              <div>
                <div className="text-xs font-semibold text-slate-200">User Service</div>
                <div className="text-[11px] font-mono text-slate-400">:8081 • userdb</div>
              </div>
            </div>
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          </div>

          <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Package className="h-4 w-4 text-purple-400" />
              <div>
                <div className="text-xs font-semibold text-slate-200">Order Service</div>
                <div className="text-[11px] font-mono text-slate-400">:8084 • orderdb</div>
              </div>
            </div>
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          </div>

          <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <CreditCard className="h-4 w-4 text-amber-400" />
              <div>
                <div className="text-xs font-semibold text-slate-200">Payment Service</div>
                <div className="text-[11px] font-mono text-slate-400">:8083 • paymentdb</div>
              </div>
            </div>
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          </div>

          <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between col-span-2 md:col-span-1">
            <div className="flex items-center gap-2.5">
              <Bell className="h-4 w-4 text-rose-400" />
              <div>
                <div className="text-xs font-semibold text-slate-200">Notification</div>
                <div className="text-[11px] font-mono text-slate-400">:8085 • notificationdb</div>
              </div>
            </div>
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          </div>
        </section>

        {/* TAB 1: WORKFLOW SIMULATOR */}
        {activeTab === 'simulator' && (
          <div className="space-y-6">
            
            {/* Simulation Controls & JWT Status */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              
              {/* Left Column: Create Order Action */}
              <div className="lg:col-span-1 bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-4">
                <div className="flex items-center justify-between">
                  <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                    <Package className="h-4 w-4 text-indigo-400" />
                    1. Simulate Order Creation
                  </h2>
                  <span className="text-[10px] px-2 py-0.5 bg-indigo-500/10 text-indigo-300 rounded border border-indigo-500/20 font-mono">
                    POST /api/v1/orders
                  </span>
                </div>

                <div className="space-y-3 text-xs">
                  <div>
                    <label className="text-slate-400 block mb-1">Authenticated Customer</label>
                    <div className="p-2.5 bg-slate-950 rounded-lg border border-slate-800 flex items-center justify-between">
                      <div>
                        <div className="font-medium text-slate-200">{currentUser.name}</div>
                        <div className="text-slate-500 text-[11px]">User ID: #{currentUser.id} • {currentUser.email}</div>
                      </div>
                      <span className="px-2 py-0.5 bg-emerald-500/10 text-emerald-400 rounded text-[10px] font-mono">JWT Verified</span>
                    </div>
                  </div>

                  <div>
                    <label className="text-slate-400 block mb-1">Select Catalog Item</label>
                    <select
                      value={selectedProduct.id}
                      onChange={(e) => {
                        const id = Number(e.target.value);
                        if (id === 1) setSelectedProduct({ id: 1, name: 'Ergonomic Mechanical Keyboard', price: 129.99 });
                        else if (id === 2) setSelectedProduct({ id: 2, name: 'Precision Wireless Mouse', price: 59.99 });
                        else setSelectedProduct({ id: 3, name: 'Noise-Cancelling Studio Headphones', price: 249.50 });
                      }}
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
                    >
                      <option value={3}>Noise-Cancelling Studio Headphones ($249.50)</option>
                      <option value={1}>Ergonomic Mechanical Keyboard ($129.99)</option>
                      <option value={2}>Precision Wireless Mouse ($59.99)</option>
                    </select>
                  </div>

                  <div className="flex gap-3">
                    <div className="w-1/3">
                      <label className="text-slate-400 block mb-1">Quantity</label>
                      <input
                        type="number"
                        min="1"
                        max="10"
                        value={itemQty}
                        onChange={(e) => setItemQty(Math.max(1, Number(e.target.value)))}
                        className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
                      />
                    </div>
                    <div className="w-2/3">
                      <label className="text-slate-400 block mb-1">Total Expected Price</label>
                      <div className="p-2.5 bg-slate-950 border border-slate-800 rounded-lg font-mono text-emerald-400 font-semibold">
                        ${(selectedProduct.price * itemQty).toFixed(2)}
                      </div>
                    </div>
                  </div>

                  <div className="p-3 bg-slate-950/60 rounded-lg border border-slate-800/80 text-[11px] text-slate-400 space-y-1">
                    <div className="flex items-center gap-1.5 text-slate-300 font-medium">
                      <Info className="h-3.5 w-3.5 text-cyan-400" />
                      V2 State Flow Guarantee
                    </div>
                    <p>Order will start in <span className="text-amber-400 font-mono">AWAITING_APPROVAL</span>. Payment is created in <span className="text-amber-400 font-mono">PENDING_APPROVAL</span>. No notification is sent yet.</p>
                  </div>

                  <button
                    id="btn-create-order"
                    onClick={handleCreateOrder}
                    disabled={isSubmittingOrder}
                    className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/20"
                  >
                    {isSubmittingOrder ? (
                      <RotateCcw className="h-4 w-4 animate-spin" />
                    ) : (
                      <Send className="h-4 w-4" />
                    )}
                    Dispatch Order through Gateway
                  </button>
                </div>
              </div>

              {/* Middle & Right: Live Orders & Payments State Machine */}
              <div className="lg:col-span-2 space-y-6">
                
                {/* Orders Panel */}
                <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-4">
                  <div className="flex items-center justify-between">
                    <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                      <Server className="h-4 w-4 text-purple-400" />
                      Order Service Ledger (`orderdb`)
                    </h2>
                    <span className="text-xs text-slate-400">{orders.length} Active Orders</span>
                  </div>

                  <div className="space-y-2.5">
                    {orders.map((order) => {
                      const payment = payments.find(p => p.orderId === order.id);
                      return (
                        <div
                          key={order.id}
                          className="p-3.5 bg-slate-950 rounded-xl border border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                        >
                          <div className="space-y-1">
                            <div className="flex items-center gap-2">
                              <span className="font-mono text-sm font-bold text-white">Order #{order.id}</span>
                              <span className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                                order.status === 'PAID'
                                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                                  : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                              }`}>
                                {order.status}
                              </span>
                              <span className="text-[11px] text-slate-500 font-mono">Trace: {order.correlationId}</span>
                            </div>
                            <div className="text-xs text-slate-400">
                              Customer: <span className="text-slate-200">{order.userName}</span> • Total: <span className="text-emerald-400 font-mono font-semibold">${order.totalAmount.toFixed(2)}</span>
                            </div>
                            <div className="text-[11px] text-slate-500">
                              Items: {order.items.map(i => `${i.quantity}x ${i.name}`).join(', ')}
                            </div>
                          </div>

                          {/* Approval Trigger Button */}
                          {payment && payment.status === 'PENDING_APPROVAL' && (
                            <button
                              id={`btn-approve-payment-${payment.id}`}
                              onClick={() => handleApprovePayment(payment.id)}
                              disabled={isApproving === payment.id}
                              className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded-lg shadow-md shadow-emerald-600/20 flex items-center gap-1.5 whitespace-nowrap self-start sm:self-center transition-colors"
                            >
                              {isApproving === payment.id ? (
                                <RotateCcw className="h-3.5 w-3.5 animate-spin" />
                              ) : (
                                <CheckCircle2 className="h-3.5 w-3.5" />
                              )}
                              Approve Payment #{payment.id}
                            </button>
                          )}

                          {payment && payment.status === 'APPROVED' && (
                            <div className="flex items-center gap-1.5 text-emerald-400 text-xs font-medium">
                              <Check className="h-4 w-4" />
                              <span>Payment Approved</span>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Notifications & Alert Log */}
                <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-4">
                  <div className="flex items-center justify-between">
                    <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                      <Bell className="h-4 w-4 text-rose-400" />
                      Notification Service Events (`notificationdb`)
                    </h2>
                    <span className="text-xs text-slate-400">Dispatched Only After Explicit Approval</span>
                  </div>

                  {notifications.length === 0 ? (
                    <div className="p-6 bg-slate-950/40 rounded-xl border border-dashed border-slate-800 text-center text-xs text-slate-500">
                      No notifications dispatched yet. As per V2 requirement, notifications are gated and sent ONLY after payment approval.
                    </div>
                  ) : (
                    <div className="space-y-2">
                      {notifications.map((n) => (
                        <div key={n.id} className="p-3 bg-slate-950 rounded-xl border border-rose-500/20 flex items-start gap-3">
                          <div className="h-7 w-7 rounded-lg bg-rose-500/10 text-rose-400 flex items-center justify-center shrink-0 mt-0.5">
                            <Bell className="h-3.5 w-3.5" />
                          </div>
                          <div className="text-xs space-y-0.5 flex-1">
                            <div className="flex items-center justify-between">
                              <span className="font-semibold text-slate-200">{n.type}</span>
                              <span className="text-[10px] font-mono text-slate-500">{n.createdAt}</span>
                            </div>
                            <p className="text-slate-300">{n.message}</p>
                            <div className="text-[10px] font-mono text-slate-500">Trace: {n.correlationId}</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

              </div>

            </div>

            {/* Live Gateway Structured HTTP Access Logs */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                  <Terminal className="h-4 w-4 text-cyan-400" />
                  Live Distributed Logs Stream (MDC Structured JSON)
                </h2>
                <button
                  onClick={() => setLogs([])}
                  className="text-xs text-slate-400 hover:text-slate-200 transition-colors"
                >
                  Clear Console
                </button>
              </div>

              <div className="bg-slate-950 rounded-xl border border-slate-800 p-3.5 font-mono text-xs max-h-56 overflow-y-auto space-y-1.5">
                {logs.map((log) => (
                  <div key={log.id} className="flex items-start gap-2 leading-relaxed">
                    <span className="text-slate-500 shrink-0">[{log.timestamp}]</span>
                    <span className={`px-1.5 py-0.2 rounded text-[10px] font-semibold shrink-0 ${
                      log.service === 'API-GATEWAY' ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20' :
                      log.service === 'ORDER-SERVICE' ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20' :
                      log.service === 'PAYMENT-SERVICE' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' :
                      'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                    }`}>
                      {log.service}
                    </span>
                    <span className="text-indigo-400 shrink-0 font-semibold">[{log.correlationId}]</span>
                    <span className={
                      log.level === 'ERROR' ? 'text-rose-400' :
                      log.level === 'WARN' ? 'text-amber-300' :
                      'text-slate-300'
                    }>
                      {log.message}
                    </span>
                  </div>
                ))}
              </div>
            </div>

          </div>
        )}

        {/* TAB 2: DISTRIBUTED TRACING & OBSERVABILITY */}
        {activeTab === 'tracing' && (
          <div className="space-y-6">
            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <Activity className="h-5 w-5 text-indigo-400" />
                    Distributed Tracing Explorer (`X-Correlation-ID`)
                  </h2>
                  <p className="text-xs text-slate-400">Filter and trace transactions across API Gateway, synchronous HTTP REST calls, and asynchronous Kafka message headers.</p>
                </div>

                <div className="flex items-center gap-2">
                  <label className="text-xs text-slate-400">Select Correlation ID:</label>
                  <select
                    value={selectedCorrelationId}
                    onChange={(e) => setSelectedCorrelationId(e.target.value)}
                    className="bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs font-mono text-indigo-300 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="all">-- All Transactions ({logs.length} logs) --</option>
                    {correlationIdsList.map(id => (
                      <option key={id} value={id}>{id}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Visual Transaction Waterfall */}
              <div className="bg-slate-950 rounded-xl border border-slate-800 p-4 space-y-3">
                <div className="text-xs font-semibold text-slate-300">Transaction Trace Waterfall</div>
                <div className="space-y-2">
                  {filteredLogs.map((l, idx) => (
                    <div key={l.id} className="p-3 bg-slate-900/70 border border-slate-800/80 rounded-lg flex items-center justify-between gap-4 text-xs font-mono">
                      <div className="flex items-center gap-3">
                        <span className="text-slate-500 w-6">#{idx + 1}</span>
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          l.service === 'API-GATEWAY' ? 'bg-cyan-500/20 text-cyan-300' :
                          l.service === 'ORDER-SERVICE' ? 'bg-purple-500/20 text-purple-300' :
                          l.service === 'PAYMENT-SERVICE' ? 'bg-amber-500/20 text-amber-300' :
                          'bg-rose-500/20 text-rose-300'
                        }`}>
                          {l.service}
                        </span>
                        <span className="text-slate-300">{l.message}</span>
                      </div>
                      <span className="text-slate-500 text-[11px] shrink-0">{l.timestamp}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: KAFKA & DLQ RESILIENCE */}
        {activeTab === 'kafka' && (
          <div className="space-y-6">
            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <Radio className="h-5 w-5 text-indigo-400" />
                    Kafka Topics, Retry Policy & Dead-Letter Queue (DLQ)
                  </h2>
                  <p className="text-xs text-slate-400">Production resilience patterns: Exponential backoff retries, header propagation, and DLQ fallbacks.</p>
                </div>

                <button
                  id="btn-simulate-dlq"
                  onClick={handleSimulateDLQRetry}
                  className="px-4 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-lg text-xs font-semibold shadow-md shadow-amber-600/20 flex items-center gap-2 transition-colors"
                >
                  <AlertTriangle className="h-4 w-4" />
                  Simulate Consumer Failure & DLQ Routing
                </button>
              </div>

              {/* Kafka Topics Summary Cards */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="p-4 bg-slate-950 rounded-xl border border-slate-800">
                  <div className="text-xs font-mono font-bold text-indigo-400">order-created-topic</div>
                  <div className="text-xs text-slate-400 mt-1">Producers: OrderService</div>
                  <div className="text-xs text-slate-400">Consumers: PaymentService</div>
                  <div className="mt-2 text-[11px] text-emerald-400 flex items-center gap-1">
                    <CheckCircle2 className="h-3 w-3" /> Healthy • Idempotent Consumer
                  </div>
                </div>

                <div className="p-4 bg-slate-950 rounded-xl border border-slate-800">
                  <div className="text-xs font-mono font-bold text-emerald-400">payment-approved-topic</div>
                  <div className="text-xs text-slate-400 mt-1">Producers: PaymentService</div>
                  <div className="text-xs text-slate-400">Consumers: OrderService, Notification</div>
                  <div className="mt-2 text-[11px] text-emerald-400 flex items-center gap-1">
                    <CheckCircle2 className="h-3 w-3" /> Healthy • Gated Notification
                  </div>
                </div>

                <div className="p-4 bg-slate-950 rounded-xl border border-slate-800">
                  <div className="text-xs font-mono font-bold text-rose-400">*.DLT (Dead Letter Topics)</div>
                  <div className="text-xs text-slate-400 mt-1">Retry: 3 attempts with 2.0x Backoff</div>
                  <div className="text-xs text-slate-400">Destination: order-created-topic.DLT</div>
                  <div className="mt-2 text-[11px] text-amber-400 flex items-center gap-1">
                    <Shield className="h-3 w-3" /> DeadLetterPublishingRecoverer Active
                  </div>
                </div>
              </div>

              {/* Kafka Events Stream */}
              <div className="bg-slate-950 rounded-xl border border-slate-800 p-4 space-y-3">
                <div className="text-xs font-semibold text-slate-300">Live Kafka Message Stream</div>
                <div className="space-y-2.5">
                  {kafkaEvents.map((evt) => (
                    <div key={evt.eventId} className="p-3.5 bg-slate-900/80 border border-slate-800 rounded-lg flex flex-col md:flex-row md:items-center justify-between gap-3 text-xs">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <span className="font-mono font-bold text-slate-200">{evt.topic}</span>
                          <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                            evt.status === 'CONSUMED' ? 'bg-emerald-500/20 text-emerald-400' :
                            evt.status === 'RETRYING' ? 'bg-amber-500/20 text-amber-300 animate-pulse' :
                            'bg-rose-500/20 text-rose-400'
                          }`}>
                            {evt.status} (Attempts: {evt.attempts})
                          </span>
                        </div>
                        <div className="text-slate-400 font-mono text-[11px]">
                          Event ID: {evt.eventId} • Correlation: <span className="text-indigo-400">{evt.correlationId}</span>
                        </div>
                        <div className="text-slate-500 font-mono text-[11px]">
                          Payload: {JSON.stringify(evt.payload)}
                        </div>
                      </div>
                      <span className="text-slate-500 font-mono text-[11px] shrink-0">{evt.timestamp}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: CODE & CONFIG EXPLORER */}
        {activeTab === 'code' && (
          <div className="space-y-6">
            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 space-y-6">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <FileCode2 className="h-5 w-5 text-indigo-400" />
                    V2 Code & Architecture Artifacts
                  </h2>
                  <p className="text-xs text-slate-400">Review production implementations of Gateway filters, payment approval logic, correlation filters, and docker deployment.</p>
                </div>

                <div className="flex items-center gap-2 p-1 bg-slate-950 rounded-lg border border-slate-800">
                  <button
                    onClick={() => setSelectedCodeSection('gateway')}
                    className={`px-3 py-1 text-xs rounded-md transition-colors ${selectedCodeSection === 'gateway' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                  >
                    Gateway & Auth
                  </button>
                  <button
                    onClick={() => setSelectedCodeSection('payment')}
                    className={`px-3 py-1 text-xs rounded-md transition-colors ${selectedCodeSection === 'payment' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                  >
                    Payment State Machine
                  </button>
                  <button
                    onClick={() => setSelectedCodeSection('correlation')}
                    className={`px-3 py-1 text-xs rounded-md transition-colors ${selectedCodeSection === 'correlation' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                  >
                    Correlation Interceptor
                  </button>
                  <button
                    onClick={() => setSelectedCodeSection('kafka-resilience')}
                    className={`px-3 py-1 text-xs rounded-md transition-colors ${selectedCodeSection === 'kafka-resilience' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                  >
                    Kafka DLQ & Retries
                  </button>
                  <button
                    onClick={() => setSelectedCodeSection('docker')}
                    className={`px-3 py-1 text-xs rounded-md transition-colors ${selectedCodeSection === 'docker' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                  >
                    Docker Compose
                  </button>
                </div>
              </div>

              {/* Code Display */}
              <div className="bg-slate-950 rounded-xl border border-slate-800 p-4 font-mono text-xs overflow-x-auto text-slate-300 leading-relaxed">
                {selectedCodeSection === 'gateway' && (
                  <pre>{`// API Gateway RouteLocator & JWT Authentication Middleware
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter) {
        return builder.routes()
            .route("user-service", r -> r.path("/api/v1/users/**")
                .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                .uri("http://user-service:8081"))
            .route("order-service", r -> r.path("/api/v1/orders/**")
                .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                .uri("http://order-service:8084"))
            .route("payment-service", r -> r.path("/api/v1/payments/**")
                .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                .uri("http://payment-service:8083"))
            .build();
    }
}`}</pre>
                )}

                {selectedCodeSection === 'payment' && (
                  <pre>{`// Payment Service: Two-Step State Machine with Explicit Approval
@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    @Transactional
    public PaymentResponse approvePayment(Long paymentId, String approvedBy) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Payment is not in PENDING_APPROVAL status");
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setTransactionId("TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Payment saved = paymentRepository.save(payment);

        // Dispatches PaymentApprovedEvent to Kafka -> Updates Order to PAID & Dispatches Notification
        paymentEventProducer.publishPaymentApprovedEvent(new PaymentApprovedEvent(
            UUID.randomUUID().toString(), saved.getId(), saved.getOrderId(),
            saved.getUserId(), saved.getAmount(), saved.getTransactionId(), approvedBy, LocalDateTime.now()
        ));
        return mapToResponse(saved);
    }
}`}</pre>
                )}

                {selectedCodeSection === 'correlation' && (
                  <pre>{`// Common Package: CorrelationIdFilter for Spring MVC & MDC propagation
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}`}</pre>
                )}

                {selectedCodeSection === 'kafka-resilience' && (
                  <pre>{`// Kafka Resilience Configuration: 3 Retries + Exponential Backoff + Dead Letter Topic
@Configuration
public class KafkaResilienceConfig {
    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        ConsumerRecordRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
            (cr, ex) -> new TopicPartition(cr.topic() + ".DLT", cr.partition()));

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxAttempts(3);
        backOff.setMaxInterval(5000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}`}</pre>
                )}

                {selectedCodeSection === 'docker' && (
                  <pre>{`# Production docker-compose.yml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    ports: ["2181:2181"]
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports: ["9092:9092"]
    depends_on: [zookeeper]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
  redis:
    image: redis:7.2-alpine
    ports: ["6379:6379"]`}</pre>
                )}
              </div>
            </div>
          </div>
        )}

      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 bg-slate-900/60 py-4 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-slate-500">
          <div>E-Commerce Microservices Platform • Version 2.0 Production Specification</div>
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5"><span className="h-1.5 w-1.5 rounded-full bg-emerald-400"></span> Gateway Active</span>
            <span className="flex items-center gap-1.5"><span className="h-1.5 w-1.5 rounded-full bg-emerald-400"></span> Kafka Broker Ready</span>
            <span className="flex items-center gap-1.5"><span className="h-1.5 w-1.5 rounded-full bg-indigo-400"></span> DLQ Active</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
