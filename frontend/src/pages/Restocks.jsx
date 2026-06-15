import { useEffect, useState } from 'react';
import { Plus, CheckCircle, Ban, Truck, Trash2 } from 'lucide-react';
import { restockApi } from '../api/client';
import { PageHeader, Card, Table, Badge, Button, Modal, Input, Loader, EmptyState } from '../components/UI';

const statusBadge = (s) => {
  const map = { PENDING: 'warning', APPROVED: 'info', DELIVERED: 'success', CANCELLED: 'danger' };
  return <Badge variant={map[s] || 'default'}>{s}</Badge>;
};

export default function Restocks() {
  const [restocks, setRestocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(false);

  const load = async () => {
    setLoading(true);
    try { setRestocks((await restockApi.getAll()).data); } catch { setRestocks([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    await restockApi.create({
      pid: fd.get('pid'),                        // ✅ custom product id
      sid: fd.get('sid'),                        // ✅ custom supplier id
      quantity: parseInt(fd.get('quantity')),
      expectedDate: fd.get('expectedDate') || null,
      createdBy: fd.get('createdBy'),
      notes: fd.get('notes'),
    });
    setModal(false);
    load();
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this restock request?')) return;
    await restockApi.delete(id);
    load();
  };

  return (
    <>
      <PageHeader title="Restocks" subtitle="Manage product restock requests">
        <Button onClick={() => setModal(true)}><Plus size={16} /> New Restock</Button>
      </PageHeader>

      <Card>
        {loading ? <Loader /> : restocks.length === 0 ? <EmptyState message="No restock requests found" /> : (
          <Table headers={['ID', 'Product (PID)', 'Supplier (SID)', 'Qty', 'Status', 'Expected', 'Actions']}>
            {restocks.map((r) => (
              <tr key={r.id} className="hover:bg-slate-700/20 transition-colors">
                <td className="py-3 px-4 text-sm font-mono text-slate-400">{r.id?.slice(0, 8)}...</td>
                {/* ✅ show pid and sid — clean custom IDs */}
                <td className="py-3 px-4 text-sm font-mono text-cyan-400">
                  {r.productId || '—'}
                </td>
                <td className="py-3 px-4 text-sm font-mono text-cyan-400">
                  {r.supplierSid || '—'}             {/* ✅ use supplierSid not supplierId */}
                </td>
                <td className="py-3 px-4 text-sm font-medium text-white">{r.quantity}</td>
                <td className="py-3 px-4">{statusBadge(r.status)}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{r.expectedDate || '—'}</td>
                <td className="py-3 px-4">
                  <div className="flex gap-1">
                    {r.status === 'PENDING' && (
                      <>
                        <Button size="sm" variant="success"
                          onClick={() => restockApi.approve(r.id).then(load)}
                          title="Approve"><CheckCircle size={14} />
                        </Button>
                        <Button size="sm" variant="danger"
                          onClick={() => restockApi.cancel(r.id).then(load)}
                          title="Cancel"><Ban size={14} />
                        </Button>
                      </>
                    )}
                    {r.status === 'APPROVED' && (
                      <Button size="sm" variant="primary"
                        onClick={() => restockApi.markDelivered(r.id).then(load)}
                        title="Mark Delivered"><Truck size={14} />
                      </Button>
                    )}
                    {(r.status === 'PENDING' || r.status === 'CANCELLED') && (
                      <Button size="sm" variant="ghost"
                        onClick={() => handleDelete(r.id)}><Trash2 size={14} />
                      </Button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        )}
      </Card>

      <Modal open={modal} onClose={() => setModal(false)} title="Create Restock Request">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Product ID (PID)" name="pid" placeholder="e.g. PROD-001" required />
          <Input label="Supplier ID (SID)" name="sid" placeholder="e.g. SUP-001" required />
          <Input label="Quantity" name="quantity" type="number" min="1" defaultValue="1" required />
          <Input label="Expected Date" name="expectedDate" type="date" />
          <Input label="Created By" name="createdBy" />
          <Input label="Notes" name="notes" />
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="ghost" onClick={() => setModal(false)}>Cancel</Button>
            <Button type="submit">Create</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}