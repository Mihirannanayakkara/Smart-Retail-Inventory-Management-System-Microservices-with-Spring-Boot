import { useEffect, useState } from 'react';
import { Trash2 } from 'lucide-react';
import { notificationApi } from '../api/client';
import { PageHeader, Card, Table, Badge, Button, Loader, EmptyState } from '../components/UI';

const typeBadge = (t) => {
  const map = {
    USER_REGISTERED: 'purple',
    ORDER_CREATED: 'info',
    ORDER_CANCELLED: 'danger',
    LOW_STOCK: 'warning',
    RESTOCK_CREATED: 'info',
    RESTOCK_COMPLETED: 'success',
  };
  return <Badge variant={map[t] || 'default'}>{t?.replace(/_/g, ' ')}</Badge>;
};

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try { setNotifications((await notificationApi.getAll()).data); } catch { setNotifications([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id) => {
    await notificationApi.delete(id);
    load();
  };

  return (
    <>
      <PageHeader title="Notifications" subtitle="System events and alerts" />

      <Card>
        {loading ? <Loader /> : notifications.length === 0 ? <EmptyState message="No notifications yet" /> : (
          <Table headers={['Type', 'Message', 'Recipient', 'Date', 'Actions']}>
            {notifications.map((n) => (
              <tr key={n.id} className="hover:bg-slate-700/20 transition-colors">
                <td className="py-3 px-4">{typeBadge(n.type)}</td>
                <td className="py-3 px-4 text-sm text-slate-300 max-w-md truncate">{n.message}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{n.recipient || '—'}</td>
                <td className="py-3 px-4 text-sm text-slate-400">{n.createdAt ? new Date(n.createdAt).toLocaleString() : '—'}</td>
                <td className="py-3 px-4">
                  <Button size="sm" variant="ghost" onClick={() => handleDelete(n.id)}><Trash2 size={14} /></Button>
                </td>
              </tr>
            ))}
          </Table>
        )}
      </Card>
    </>
  );
}
