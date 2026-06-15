import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { supplierApi } from '../api/client';
import { PageHeader, Card, Table, Badge, Button, Modal, Input, Loader, EmptyState } from '../components/UI';

export default function Suppliers() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState({ open: false, data: null });

  const load = async () => {
    setLoading(true);
    try { setSuppliers((await supplierApi.getAll()).data); } catch { setSuppliers([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id) => {
    if (!confirm('Delete this supplier?')) return;
    await supplierApi.delete(id);
    load();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const payload = {
      sid: fd.get('sid'),                                    // ✅ NEW
      name: fd.get('name'),
      company: fd.get('company'),
      email: fd.get('email'),
      phone: fd.get('phone'),
      address: fd.get('address'),
    };
    if (modal.data?.id) {
      await supplierApi.update(modal.data.id, payload);
    } else {
      await supplierApi.create(payload);
    }
    setModal({ open: false, data: null });
    load();
  };

  return (
    <>
      <PageHeader title="Suppliers" subtitle="Manage your supply chain partners">
        <Button onClick={() => setModal({ open: true, data: null })}><Plus size={16} /> Add Supplier</Button>
      </PageHeader>

      <Card>
        {loading ? <Loader /> : suppliers.length === 0 ? <EmptyState message="No suppliers found" /> : (
          <Table headers={['SID', 'Name', 'Company', 'Email', 'Phone', 'Status', 'Actions']}>
            {suppliers.map((s) => (
              <tr key={s.id} className="hover:bg-slate-700/20 transition-colors">
                <td className="py-3 px-4 text-sm font-mono text-cyan-400">{s.sid}</td>  {/* ✅ NEW */}
                <td className="py-3 px-4 text-sm font-medium text-white">{s.name}</td>
                <td className="py-3 px-4 text-sm text-slate-300">{s.company}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{s.email}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{s.phone || '—'}</td>
                <td className="py-3 px-4">
                  <Badge variant={s.status === 'ACTIVE' ? 'success' : 'danger'}>{s.status}</Badge>
                </td>
                <td className="py-3 px-4">
                  <div className="flex gap-1">
                    <Button size="sm" variant="ghost" onClick={() => setModal({ open: true, data: s })}><Pencil size={14} /></Button>
                    <Button size="sm" variant="ghost" onClick={() => handleDelete(s.id)}><Trash2 size={14} /></Button>
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        )}
      </Card>

      <Modal open={modal.open} onClose={() => setModal({ open: false, data: null })} title={modal.data?.id ? 'Edit Supplier' : 'Add Supplier'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* ✅ NEW: sid field */}
          {!modal.data?.id && (
            <Input label="Supplier ID (SID)" name="sid" placeholder="e.g. SUP-001" required />
          )}
          {modal.data?.id && (
            <Input label="Supplier ID (SID)" name="sid" defaultValue={modal.data?.sid} readOnly />
          )}
          <Input label="Name" name="name" defaultValue={modal.data?.name} required />
          <Input label="Company" name="company" defaultValue={modal.data?.company} required />
          <Input label="Email" name="email" type="email" defaultValue={modal.data?.email} required />
          <Input label="Phone" name="phone" defaultValue={modal.data?.phone} />
          <Input label="Address" name="address" defaultValue={modal.data?.address} />
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="ghost" onClick={() => setModal({ open: false, data: null })}>Cancel</Button>
            <Button type="submit">{modal.data?.id ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}