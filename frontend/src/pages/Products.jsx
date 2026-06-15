import { useEffect, useState } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import { productApi } from '../api/client';
import { PageHeader, Card, Table, Badge, Button, Modal, Input, Loader, EmptyState } from '../components/UI';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modal, setModal] = useState({ open: false, data: null });

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await productApi.getAll();
      setProducts(data);
    } catch { setProducts([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id) => {
    if (!confirm('Delete this product?')) return;
    await productApi.delete(id);
    load();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const payload = {
      pid: fd.get('pid'),                                    // ✅ NEW
      name: fd.get('name'),
      sku: fd.get('sku'),
      category: fd.get('category'),
      price: parseFloat(fd.get('price')),
      quantity: parseInt(fd.get('quantity')),
      lowStockThreshold: parseInt(fd.get('lowStockThreshold')),
      description: fd.get('description'),
    };
    if (modal.data?.id) {
      await productApi.update(modal.data.id, payload);
    } else {
      await productApi.create(payload);
    }
    setModal({ open: false, data: null });
    load();
  };

  const filtered = products.filter((p) =>
    p.name?.toLowerCase().includes(search.toLowerCase()) ||
    p.sku?.toLowerCase().includes(search.toLowerCase()) ||
    p.category?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <PageHeader title="Products" subtitle="Manage your product inventory">
        <div className="relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            placeholder="Search products..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 pr-4 py-2 bg-slate-800/60 border border-slate-700/50 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 w-64"
          />
        </div>
        <Button onClick={() => setModal({ open: true, data: null })}><Plus size={16} /> Add Product</Button>
      </PageHeader>

      <Card>
        {loading ? <Loader /> : filtered.length === 0 ? <EmptyState message="No products found" /> : (
          <Table headers={['PID', 'Name', 'SKU', 'Category', 'Price', 'Stock', 'Status', 'Actions']}>
            {filtered.map((p) => (
              <tr key={p.id} className="hover:bg-slate-700/20 transition-colors">
                <td className="py-3 px-4 text-sm font-mono text-cyan-400">{p.pid}</td>  {/* ✅ NEW */}
                <td className="py-3 px-4 text-sm font-medium text-white">{p.name}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{p.sku}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{p.category}</td>
                <td className="py-3 px-4 text-sm text-slate-300">${parseFloat(p.price).toFixed(2)}</td>
                <td className="py-3 px-4 text-sm text-slate-300">{p.quantity}</td>
                <td className="py-3 px-4">
                  <Badge variant={p.lowStock ? 'danger' : 'success'}>
                    {p.lowStock ? 'Low Stock' : 'In Stock'}
                  </Badge>
                </td>
                <td className="py-3 px-4">
                  <div className="flex gap-1">
                    <Button size="sm" variant="ghost" onClick={() => setModal({ open: true, data: p })}><Pencil size={14} /></Button>
                    <Button size="sm" variant="ghost" onClick={() => handleDelete(p.id)}><Trash2 size={14} /></Button>
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        )}
      </Card>

      <Modal open={modal.open} onClose={() => setModal({ open: false, data: null })} title={modal.data?.id ? 'Edit Product' : 'Add Product'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* ✅ NEW: pid field — only show on create, not edit */}
          {!modal.data?.id && (
            <Input label="Product ID (PID)" name="pid" placeholder="e.g. PROD-001" required />
          )}
          {/* Show pid as readonly on edit */}
          {modal.data?.id && (
            <Input label="Product ID (PID)" name="pid" defaultValue={modal.data?.pid} readOnly />
          )}
          <Input label="Name" name="name" defaultValue={modal.data?.name} required />
          <Input label="SKU" name="sku" defaultValue={modal.data?.sku} required />
          <Input label="Category" name="category" defaultValue={modal.data?.category} required />
          <div className="grid grid-cols-2 gap-4">
            <Input label="Price" name="price" type="number" step="0.01" defaultValue={modal.data?.price} required />
            <Input label="Quantity" name="quantity" type="number" defaultValue={modal.data?.quantity} required />
          </div>
          <Input label="Low Stock Threshold" name="lowStockThreshold" type="number" defaultValue={modal.data?.lowStockThreshold} required />
          <Input label="Description" name="description" defaultValue={modal.data?.description} />
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="ghost" onClick={() => setModal({ open: false, data: null })}>Cancel</Button>
            <Button type="submit">{modal.data?.id ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}