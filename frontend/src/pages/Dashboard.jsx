import { useEffect, useState } from 'react';
import { Package, ShoppingCart, Users, Truck, Bell, AlertTriangle } from 'lucide-react';
import { productApi, orderApi, userApi, supplierApi, notificationApi } from '../api/client';
import { PageHeader, StatCard, Card, Loader } from '../components/UI';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [products, orders, users, suppliers, notifications, lowStock] = await Promise.allSettled([
          productApi.getAll(),
          orderApi.getAll(),
          userApi.getAll(),
          supplierApi.getAll(),
          notificationApi.getAll(),
          productApi.getLowStock(),
        ]);
        setStats({
          products: products.status === 'fulfilled' ? products.value.data.length : 0,
          orders: orders.status === 'fulfilled' ? orders.value.data.length : 0,
          users: users.status === 'fulfilled' ? users.value.data.length : 0,
          suppliers: suppliers.status === 'fulfilled' ? suppliers.value.data.length : 0,
          notifications: notifications.status === 'fulfilled' ? notifications.value.data.length : 0,
          lowStock: lowStock.status === 'fulfilled' ? lowStock.value.data.length : 0,
        });
      } catch {
        setStats({ products: 0, orders: 0, users: 0, suppliers: 0, notifications: 0, lowStock: 0 });
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <Loader />;

  return (
    <>
      <PageHeader title="Dashboard" subtitle="Overview of your retail system" />
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
        <StatCard icon={Package} label="Total Products" value={stats.products} color="cyan" />
        <StatCard icon={ShoppingCart} label="Total Orders" value={stats.orders} color="blue" />
        <StatCard icon={Users} label="Total Users" value={stats.users} color="purple" />
        <StatCard icon={Truck} label="Suppliers" value={stats.suppliers} color="green" />
        <StatCard icon={Bell} label="Notifications" value={stats.notifications} color="yellow" />
        <StatCard icon={AlertTriangle} label="Low Stock Alerts" value={stats.lowStock} color="red" />
      </div>

      <div className="mt-8">
        <Card className="p-6">
          <h2 className="text-lg font-semibold text-white mb-2">Welcome to Smart Retail</h2>
          <p className="text-slate-400 text-sm leading-relaxed">
            Manage your inventory, track orders, coordinate with suppliers, and monitor restocking —
            all from one unified dashboard. Use the sidebar to navigate between modules.
          </p>
        </Card>
      </div>
    </>
  );
}
