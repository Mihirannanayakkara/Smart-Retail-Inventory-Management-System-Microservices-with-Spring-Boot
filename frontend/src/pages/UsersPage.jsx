import { useEffect, useState } from 'react';
import { Plus, UserCheck, UserX, Trash2 } from 'lucide-react';
import { userApi } from '../api/client';
import { PageHeader, Card, Table, Badge, Button, Modal, Input, Select, Loader, EmptyState } from '../components/UI';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(false);

  const load = async () => {
    setLoading(true);
    try { setUsers((await userApi.getAll()).data); } catch { setUsers([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    await userApi.register({ name: fd.get('name'), email: fd.get('email'), password: fd.get('password'), role: fd.get('role') });
    setModal(false);
    load();
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this user?')) return;
    await userApi.delete(id);
    load();
  };

  return (
    <>
      <PageHeader title="Users" subtitle="Manage system users and roles">
        <Button onClick={() => setModal(true)}><Plus size={16} /> Register User</Button>
      </PageHeader>

      <Card>
        {loading ? <Loader /> : users.length === 0 ? <EmptyState message="No users found" /> : (
          <Table headers={['Name', 'Email', 'Role', 'Status', 'Created', 'Actions']}>
            {users.map((u) => (
              <tr key={u.id} className="hover:bg-slate-700/20 transition-colors">
                <td className="py-3 px-4 text-sm font-medium text-white">{u.name}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{u.email}</td>
                <td className="py-3 px-4"><Badge variant="purple">{u.role}</Badge></td>
                <td className="py-3 px-4">
                  <Badge variant={u.status === 'ACTIVE' ? 'success' : 'danger'}>{u.status}</Badge>
                </td>
                <td className="py-3 px-4 text-sm text-slate-400">{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                <td className="py-3 px-4">
                  <div className="flex gap-1">
                    {u.status === 'ACTIVE' ? (
                      <Button size="sm" variant="ghost" onClick={() => userApi.deactivate(u.id).then(load)}><UserX size={14} /></Button>
                    ) : (
                      <Button size="sm" variant="ghost" onClick={() => userApi.activate(u.id).then(load)}><UserCheck size={14} /></Button>
                    )}
                    <Button size="sm" variant="ghost" onClick={() => handleDelete(u.id)}><Trash2 size={14} /></Button>
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        )}
      </Card>

      <Modal open={modal} onClose={() => setModal(false)} title="Register User">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Name" name="name" required />
          <Input label="Email" name="email" type="email" required />
          <Input label="Password" name="password" type="password" required />
          <Select label="Role" name="role" options={[
            { value: 'ADMIN', label: 'Admin' },
            { value: 'MANAGER', label: 'Manager' },
            { value: 'STAFF', label: 'Staff' },
          ]} />
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="ghost" onClick={() => setModal(false)}>Cancel</Button>
            <Button type="submit">Register</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}
