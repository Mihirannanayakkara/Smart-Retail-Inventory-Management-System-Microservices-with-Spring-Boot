import { useEffect, useMemo, useState } from 'react';
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Ban,
  CheckCircle,
  Trash2,
  ChevronLeft,
  ChevronRight,
  Minus,
} from 'lucide-react';
import { orderApi, productApi, userApi } from '../api/client';
import {
  PageHeader,
  Card,
  Table,
  Badge,
  Button,
  Modal,
  Input,
  Select,
  Loader,
  EmptyState,
  TextArea,
} from '../components/UI';

const PAGE_SIZE = 5;
const ORDER_STATUSES = ['ALL', 'PLACED', 'COMPLETED', 'CANCELLED'];
const PAYMENT_METHODS = [
  { value: 'CARD', label: 'Card' },
  { value: 'CASH_ON_DELIVERY', label: 'Cash on Delivery' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
];

const emptyForm = {
  userId: '',
  email: '',
  shippingAddress: '',
  paymentMethod: 'CARD',
  notes: '',
  items: [{ productId: '', quantity: 1 }],
};

const currency = (value) => `$${Number(value || 0).toFixed(2)}`;

const statusBadge = (status) => {
  const map = {
    PLACED: 'info',
    COMPLETED: 'success',
    CANCELLED: 'danger',
  };
  return <Badge variant={map[status] || 'default'}>{status}</Badge>;
};

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [users, setUsers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(1);
  const [error, setError] = useState('');
  const [formModal, setFormModal] = useState({ open: false, mode: 'create', orderId: null });
  const [detailsModal, setDetailsModal] = useState({ open: false, order: null });
  const [form, setForm] = useState(emptyForm);

  const userMap = useMemo(() => Object.fromEntries(users.map((u) => [u.id, u])), [users]);
  const productMap = useMemo(() => Object.fromEntries(products.map((p) => [p.id, p])), [products]);

  const orderSummary = useMemo(() => ({
    totalOrders: orders.length,
    placed: orders.filter((order) => order.status === 'PLACED').length,
    completed: orders.filter((order) => order.status === 'COMPLETED').length,
    cancelled: orders.filter((order) => order.status === 'CANCELLED').length,
  }), [orders]);

  const formComputed = useMemo(() => {
    const normalizedItems = form.items.map((item) => {
      const product = productMap[item.productId];
      const quantity = Number(item.quantity || 0);
      const unitPrice = Number(product?.price || 0);
      return {
        ...item,
        productName: product?.name || '',
        sku: product?.sku || '',
        unitPrice,
        lineTotal: unitPrice * quantity,
        availableStock: product?.quantity ?? 0,
      };
    });

    const subtotal = normalizedItems.reduce((sum, item) => sum + item.lineTotal, 0);
    return {
      items: normalizedItems,
      subtotal,
      shippingFee: 0,
      taxAmount: 0,
      total: subtotal,
    };
  }, [form.items, productMap]);

  const filteredOrders = useMemo(() => {
    const query = search.trim().toLowerCase();
    return orders.filter((order) => {
      const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
      const matchesSearch = !query || [
        order.id,
        order.userId,
        order.customerName,
        order.customerEmail,
        order.paymentMethod,
      ].some((value) => String(value || '').toLowerCase().includes(query));
      return matchesStatus && matchesSearch;
    });
  }, [orders, search, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredOrders.length / PAGE_SIZE));
  const paginatedOrders = filteredOrders.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  useEffect(() => {
    loadAll();
  }, []);

  useEffect(() => {
    setPage(1);
  }, [search, statusFilter]);

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

  const loadAll = async () => {
    setLoading(true);
    setError('');
    try {
      const [ordersRes, usersRes, productsRes] = await Promise.all([
        orderApi.getAll(),
        userApi.getAll(),
        productApi.getAll(),
      ]);
      setOrders(ordersRes.data || []);
      setUsers(usersRes.data || []);
      setProducts(productsRes.data || []);
    } catch (err) {
      setOrders([]);
      setUsers([]);
      setProducts([]);
      setError(err?.response?.data?.message || 'Failed to load order data.');
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setError('');
    setForm(emptyForm);
    setFormModal({ open: true, mode: 'create', orderId: null });
  };

  const openEditModal = (order) => {
    setError('');
    setForm({
      userId: order.userId || '',
      email: order.customerEmail || '',
      shippingAddress: order.shippingAddress || '',
      paymentMethod: order.paymentMethod || 'CARD',
      notes: order.notes || '',
      items: (order.items || []).map((item) => ({
        productId: item.productId || '',
        quantity: item.quantity || 1,
      })),
    });
    setFormModal({ open: true, mode: 'edit', orderId: order.id });
  };

  const openDetailsModal = async (orderId) => {
    try {
      const { data } = await orderApi.getById(orderId);
      setDetailsModal({ open: true, order: data });
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to load order details.');
    }
  };

  const closeFormModal = () => {
    setFormModal({ open: false, mode: 'create', orderId: null });
    setForm(emptyForm);
    setError('');
  };

  const updateFormField = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleCustomerChange = (userId) => {
    const selectedUser = userMap[userId];
    setForm((prev) => ({
      ...prev,
      userId,
      email: selectedUser?.email || '',
    }));
  };

  const updateItem = (index, field, value) => {
    setForm((prev) => ({
      ...prev,
      items: prev.items.map((item, idx) => idx === index ? { ...item, [field]: value } : item),
    }));
  };

  const addItemRow = () => {
    setForm((prev) => ({
      ...prev,
      items: [...prev.items, { productId: '', quantity: 1 }],
    }));
  };

  const removeItemRow = (index) => {
    setForm((prev) => ({
      ...prev,
      items: prev.items.length === 1 ? prev.items : prev.items.filter((_, idx) => idx !== index),
    }));
  };

  const validateForm = () => {
    if (!form.userId.trim()) return 'Customer ID is required.';
    if (!form.email.trim()) return 'Customer email is required.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) return 'Enter a valid customer email.';
    if (!form.shippingAddress.trim()) return 'Shipping address is required.';
    if (!form.paymentMethod.trim()) return 'Payment method is required.';
    if (!form.items.length) return 'Add at least one order item.';

    const selectedUser = userMap[form.userId];
    if (!selectedUser) return 'Selected customer does not exist.';
    if (selectedUser.email?.toLowerCase() !== form.email.trim().toLowerCase()) {
      return 'Customer email must match the selected customer ID.';
    }

    const seen = new Set();
    for (const item of form.items) {
      if (!item.productId) return 'Every order item must have a product.';
      if (seen.has(item.productId)) return 'Do not repeat the same product in multiple rows.';
      seen.add(item.productId);

      const qty = Number(item.quantity);
      if (!Number.isInteger(qty) || qty < 1) return 'Quantity must be at least 1.';

      const product = productMap[item.productId];
      if (!product) return 'One or more selected products no longer exist.';
    }

    return '';
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const validationMessage = validateForm();
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    const payload = {
      userId: form.userId.trim(),
      email: form.email.trim(),
      shippingAddress: form.shippingAddress.trim(),
      paymentMethod: form.paymentMethod.trim(),
      notes: form.notes.trim(),
      items: form.items.map((item) => ({
        productId: item.productId,
        quantity: Number(item.quantity),
      })),
    };

    try {
      setSaving(true);
      setError('');
      if (formModal.mode === 'edit') {
        await orderApi.update(formModal.orderId, payload);
      } else {
        await orderApi.create(payload);
      }
      closeFormModal();
      await loadAll();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save order.');
    } finally {
      setSaving(false);
    }
  };

  const handleComplete = async (orderId) => {
    if (!confirm('Complete this order?')) return;
    try {
      await orderApi.complete(orderId);
      await loadAll();
      if (detailsModal.order?.id === orderId) {
        openDetailsModal(orderId);
      }
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to complete order.');
    }
  };

  const handleCancel = async (orderId) => {
    if (!confirm('Cancel this order? This acts as a soft delete.')) return;
    try {
      await orderApi.delete(orderId);
      await loadAll();
      if (detailsModal.order?.id === orderId) {
        setDetailsModal({ open: false, order: null });
      }
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to cancel order.');
    }
  };

  return (
    <>
      <PageHeader title="Orders" subtitle="Place, track, edit, complete, and cancel customer orders from one screen.">
        <Button onClick={openCreateModal}><Plus size={16} /> Place Order</Button>
      </PageHeader>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <MetricCard label="Total Orders" value={orderSummary.totalOrders} />
        <MetricCard label="Placed" value={orderSummary.placed} variant="info" />
        <MetricCard label="Completed" value={orderSummary.completed} variant="success" />
        <MetricCard label="Cancelled" value={orderSummary.cancelled} variant="danger" />
      </div>

      <Card className="mb-6 p-4">
        <div className="flex flex-col md:flex-row gap-4 md:items-center md:justify-between">
          <div className="relative w-full md:w-80">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by order ID, customer, or email..."
              className="w-full pl-9 pr-4 py-2 bg-slate-900/50 border border-slate-600/50 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50"
            />
          </div>
          <div className="w-full md:w-56">
            <Select
              label=""
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              options={ORDER_STATUSES.map((status) => ({ value: status, label: status === 'ALL' ? 'All Statuses' : status }))}
            />
          </div>
        </div>
      </Card>

      <Card>
        {loading ? <Loader /> : filteredOrders.length === 0 ? <EmptyState message="No orders found." /> : (
          <>
            <Table headers={['Order ID', 'Customer ID', 'Total Price', 'Status', 'Actions']}>
              {paginatedOrders.map((order) => {
                const isPlaced = order.status === 'PLACED';
                return (
                  <tr key={order.id} className="hover:bg-slate-700/20 transition-colors">
                    <td className="py-3 px-4 text-sm">
                      <button
                        type="button"
                        onClick={() => openDetailsModal(order.id)}
                        className="font-mono text-cyan-400 hover:text-cyan-300 underline underline-offset-4"
                      >
                        {order.id}
                      </button>
                    </td>
                    <td className="py-3 px-4 text-sm text-slate-300">{order.userId}</td>
                    <td className="py-3 px-4 text-sm font-medium text-white">{currency(order.totalAmount)}</td>
                    <td className="py-3 px-4">{statusBadge(order.status)}</td>
                    <td className="py-3 px-4">
                      <div className="flex flex-wrap gap-2">
                        <Button size="sm" variant="ghost" onClick={() => openDetailsModal(order.id)}><Eye size={14} /></Button>
                        <Button size="sm" variant="ghost" onClick={() => openEditModal(order)} disabled={!isPlaced}><Pencil size={14} /></Button>
                        <Button size="sm" variant="success" onClick={() => handleComplete(order.id)} disabled={!isPlaced}><CheckCircle size={14} /></Button>
                        <Button size="sm" variant="danger" onClick={() => handleCancel(order.id)} disabled={!isPlaced}><Trash2 size={14} /></Button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </Table>

            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 px-4 py-4 border-t border-slate-700/40">
              <p className="text-sm text-slate-400">
                Showing {(page - 1) * PAGE_SIZE + 1} to {Math.min(page * PAGE_SIZE, filteredOrders.length)} of {filteredOrders.length} orders
              </p>
              <div className="flex items-center gap-2">
                <Button variant="ghost" size="sm" onClick={() => setPage((prev) => Math.max(1, prev - 1))} disabled={page === 1}>
                  <ChevronLeft size={14} /> Prev
                </Button>
                <span className="text-sm text-slate-300">Page {page} of {totalPages}</span>
                <Button variant="ghost" size="sm" onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))} disabled={page === totalPages}>
                  Next <ChevronRight size={14} />
                </Button>
              </div>
            </div>
          </>
        )}
      </Card>

      <Modal
        open={formModal.open}
        onClose={closeFormModal}
        title={formModal.mode === 'edit' ? 'Edit Order' : 'Place Order'}
        maxWidthClass="max-w-4xl"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">{error}</div>}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Customer ID"
              value={form.userId}
              onChange={(e) => handleCustomerChange(e.target.value)}
              options={[
                { value: '', label: 'Select customer' },
                ...users.map((user) => ({ value: user.id, label: `${user.id} — ${user.name}` })),
              ]}
            />
            <Input
              label="Email"
              type="email"
              value={form.email}
              onChange={(e) => updateFormField('email', e.target.value)}
              placeholder="customer@example.com"
            />
            <Input
              label="Shipping Address"
              value={form.shippingAddress}
              onChange={(e) => updateFormField('shippingAddress', e.target.value)}
              placeholder="Street, city, postal code"
            />
            <Select
              label="Payment Method"
              value={form.paymentMethod}
              onChange={(e) => updateFormField('paymentMethod', e.target.value)}
              options={PAYMENT_METHODS}
            />
          </div>

          <TextArea
            label="Notes"
            rows={3}
            value={form.notes}
            onChange={(e) => updateFormField('notes', e.target.value)}
            placeholder="Optional order notes"
          />

          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-sm font-semibold text-white">Order Items</h3>
                <p className="text-xs text-slate-400">Add one or more products and quantities. Total updates automatically.</p>
              </div>
              <Button variant="ghost" size="sm" onClick={addItemRow}><Plus size={14} /> Add Item</Button>
            </div>

            <div className="space-y-3">
              {formComputed.items.map((item, index) => (
                <div key={`${item.productId}-${index}`} className="grid grid-cols-1 lg:grid-cols-[1.8fr_0.8fr_0.8fr_0.9fr_auto] gap-3 items-end rounded-xl border border-slate-700/40 p-4 bg-slate-900/30">
                  <Select
                    label={`Product ${index + 1}`}
                    value={item.productId}
                    onChange={(e) => updateItem(index, 'productId', e.target.value)}
                    options={[
                      { value: '', label: 'Select product' },
                      ...products.map((product) => ({ value: product.id, label: `${product.name} (${product.sku})` })),
                    ]}
                  />
                  <Input
                    label="Quantity"
                    type="number"
                    min="1"
                    value={item.quantity}
                    onChange={(e) => updateItem(index, 'quantity', e.target.value)}
                  />
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-1.5">Unit Price</label>
                    <div className="rounded-lg border border-slate-700/50 bg-slate-950/60 px-3 py-2 text-sm text-slate-200">{currency(item.unitPrice)}</div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-1.5">Line Total</label>
                    <div className="rounded-lg border border-slate-700/50 bg-slate-950/60 px-3 py-2 text-sm text-white">{currency(item.lineTotal)}</div>
                  </div>
                  <Button variant="ghost" size="sm" onClick={() => removeItemRow(index)} disabled={form.items.length === 1}>
                    <Minus size={14} />
                  </Button>
                  {item.productId && (
                    <div className="lg:col-span-5 text-xs text-slate-400 -mt-1">
                      Available stock: {item.availableStock} {item.sku ? `• SKU: ${item.sku}` : ''}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <PricingBox label="Subtotal" value={currency(formComputed.subtotal)} />
            <PricingBox label="Shipping" value={currency(formComputed.shippingFee)} />
            <PricingBox label="Total" value={currency(formComputed.total)} highlight />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button variant="ghost" onClick={closeFormModal}>Close</Button>
            <Button type="submit" disabled={saving}>{saving ? 'Saving...' : formModal.mode === 'edit' ? 'Update Order' : 'Place Order'}</Button>
          </div>
        </form>
      </Modal>

      <Modal
        open={detailsModal.open}
        onClose={() => setDetailsModal({ open: false, order: null })}
        title="Order Details"
        maxWidthClass="max-w-5xl"
      >
        {!detailsModal.order ? <Loader /> : (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <InfoBox label="Order ID" value={detailsModal.order.id} mono />
              <InfoBox label="Status" valueNode={statusBadge(detailsModal.order.status)} />
              <InfoBox label="Payment Method" value={detailsModal.order.paymentMethod || '—'} />
              <InfoBox label="Customer ID" value={detailsModal.order.userId} mono />
              <InfoBox label="Customer Name" value={detailsModal.order.customerName || '—'} />
              <InfoBox label="Customer Email" value={detailsModal.order.customerEmail || '—'} />
              <InfoBox label="Created At" value={detailsModal.order.createdAt ? new Date(detailsModal.order.createdAt).toLocaleString() : '—'} />
              <InfoBox label="Updated At" value={detailsModal.order.updatedAt ? new Date(detailsModal.order.updatedAt).toLocaleString() : '—'} />
              <InfoBox label="Shipping Address" value={detailsModal.order.shippingAddress || '—'} />
            </div>

            <Card className="overflow-hidden">
              <div className="px-4 py-3 border-b border-slate-700/40">
                <h3 className="text-sm font-semibold text-white">Items</h3>
              </div>
              <Table headers={['Product', 'Product ID', 'Qty', 'Unit Price', 'Subtotal']}>
                {(detailsModal.order.items || []).map((item, index) => (
                  <tr key={`${item.productId}-${index}`}>
                    <td className="py-3 px-4 text-sm text-white">{item.productName}</td>
                    <td className="py-3 px-4 text-sm font-mono text-slate-400">{item.productId}</td>
                    <td className="py-3 px-4 text-sm text-slate-300">{item.quantity}</td>
                    <td className="py-3 px-4 text-sm text-slate-300">{currency(item.unitPrice)}</td>
                    <td className="py-3 px-4 text-sm font-medium text-white">{currency(item.subTotal)}</td>
                  </tr>
                ))}
              </Table>
            </Card>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <Card className="p-4">
                <h3 className="text-sm font-semibold text-white mb-3">Pricing Breakdown</h3>
                <div className="space-y-2 text-sm">
                  <DetailRow label="Subtotal" value={currency(detailsModal.order.subTotal)} />
                  <DetailRow label="Shipping Fee" value={currency(detailsModal.order.shippingFee)} />
                  <DetailRow label="Tax" value={currency(detailsModal.order.taxAmount)} />
                  <DetailRow label="Total Price" value={currency(detailsModal.order.totalAmount)} strong />
                </div>
              </Card>

              <Card className="p-4">
                <h3 className="text-sm font-semibold text-white mb-3">Notes</h3>
                <p className="text-sm text-slate-300 whitespace-pre-wrap">{detailsModal.order.notes || 'No notes provided.'}</p>
              </Card>
            </div>

            <div className="flex flex-wrap justify-end gap-3 pt-2">
              <Button variant="ghost" onClick={() => setDetailsModal({ open: false, order: null })}>Close</Button>
              <Button
                variant="ghost"
                onClick={() => {
                  setDetailsModal({ open: false, order: null });
                  openEditModal(detailsModal.order);
                }}
                disabled={detailsModal.order.status !== 'PLACED'}
              >
                <Pencil size={14} /> Edit
              </Button>
              <Button
                variant="success"
                onClick={() => handleComplete(detailsModal.order.id)}
                disabled={detailsModal.order.status !== 'PLACED'}
              >
                <CheckCircle size={14} /> Complete Order
              </Button>
              <Button
                variant="danger"
                onClick={() => handleCancel(detailsModal.order.id)}
                disabled={detailsModal.order.status !== 'PLACED'}
              >
                <Ban size={14} /> Cancel Order
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </>
  );
}

function MetricCard({ label, value, variant = 'default' }) {
  const styles = {
    default: 'border-slate-700/50 bg-slate-800/50 text-white',
    info: 'border-cyan-500/20 bg-cyan-500/10 text-cyan-300',
    success: 'border-emerald-500/20 bg-emerald-500/10 text-emerald-300',
    danger: 'border-rose-500/20 bg-rose-500/10 text-rose-300',
  };

  return (
    <div className={`rounded-xl border p-4 ${styles[variant]}`}>
      <p className="text-xs uppercase tracking-wider text-slate-400">{label}</p>
      <p className="mt-2 text-2xl font-bold">{value}</p>
    </div>
  );
}

function PricingBox({ label, value, highlight = false }) {
  return (
    <div className={`rounded-xl border p-4 ${highlight ? 'border-cyan-500/30 bg-cyan-500/10' : 'border-slate-700/40 bg-slate-900/40'}`}>
      <p className="text-xs uppercase tracking-wider text-slate-400">{label}</p>
      <p className="mt-2 text-lg font-semibold text-white">{value}</p>
    </div>
  );
}

function InfoBox({ label, value, valueNode, mono = false }) {
  return (
    <Card className="p-4">
      <p className="text-xs uppercase tracking-wider text-slate-400">{label}</p>
      <div className={`mt-2 text-sm text-white break-all ${mono ? 'font-mono' : ''}`}>{valueNode || value}</div>
    </Card>
  );
}

function DetailRow({ label, value, strong = false }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-slate-400">{label}</span>
      <span className={strong ? 'font-semibold text-white' : 'text-slate-200'}>{value}</span>
    </div>
  );
}
