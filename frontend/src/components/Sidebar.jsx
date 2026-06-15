import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Package, ShoppingCart, Users, Truck,
  RotateCcw, Bell,
} from 'lucide-react';

const links = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/products', icon: Package, label: 'Products' },
  { to: '/orders', icon: ShoppingCart, label: 'Orders' },
  { to: '/users', icon: Users, label: 'Users' },
  { to: '/suppliers', icon: Truck, label: 'Suppliers' },
  { to: '/restocks', icon: RotateCcw, label: 'Restocks' },
  { to: '/notifications', icon: Bell, label: 'Notifications' },
];

export default function Sidebar() {
  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-gradient-to-b from-slate-900 to-slate-800 text-white flex flex-col z-50">
      <div className="p-6 border-b border-slate-700/50">
        <h1 className="text-xl font-bold tracking-tight bg-gradient-to-r from-cyan-400 to-blue-500 bg-clip-text text-transparent">
          Smart Retail
        </h1>
        <p className="text-xs text-slate-400 mt-1">Inventory Management</p>
      </div>
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-cyan-500/15 text-cyan-400 shadow-sm shadow-cyan-500/10'
                  : 'text-slate-300 hover:bg-slate-700/50 hover:text-white'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t border-slate-700/50 text-xs text-slate-500">
        © 2026 Smart Retail System
      </div>
    </aside>
  );
}
