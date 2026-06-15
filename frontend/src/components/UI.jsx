export function PageHeader({ title, subtitle, children }) {
  return (
    <div className="flex items-center justify-between mb-8">
      <div>
        <h1 className="text-2xl font-bold text-white">{title}</h1>
        {subtitle && <p className="text-sm text-slate-400 mt-1">{subtitle}</p>}
      </div>
      {children && <div className="flex items-center gap-3">{children}</div>}
    </div>
  );
}

export function Card({ children, className = '' }) {
  return (
    <div className={`bg-slate-800/50 border border-slate-700/50 rounded-xl backdrop-blur-sm ${className}`}>
      {children}
    </div>
  );
}

export function StatCard({ icon: Icon, label, value, color = 'cyan' }) {
  const colors = {
    cyan: 'from-cyan-500/20 to-cyan-500/5 text-cyan-400 border-cyan-500/20',
    green: 'from-emerald-500/20 to-emerald-500/5 text-emerald-400 border-emerald-500/20',
    yellow: 'from-amber-500/20 to-amber-500/5 text-amber-400 border-amber-500/20',
    red: 'from-rose-500/20 to-rose-500/5 text-rose-400 border-rose-500/20',
    purple: 'from-violet-500/20 to-violet-500/5 text-violet-400 border-violet-500/20',
    blue: 'from-blue-500/20 to-blue-500/5 text-blue-400 border-blue-500/20',
  };
  return (
    <div className={`bg-gradient-to-br ${colors[color]} border rounded-xl p-5`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-medium text-slate-400 uppercase tracking-wide">{label}</p>
          <p className="text-2xl font-bold mt-1">{value}</p>
        </div>
        <Icon size={28} className="opacity-60" />
      </div>
    </div>
  );
}

export function Badge({ children, variant = 'default' }) {
  const variants = {
    default: 'bg-slate-700 text-slate-300',
    success: 'bg-emerald-500/15 text-emerald-400',
    warning: 'bg-amber-500/15 text-amber-400',
    danger: 'bg-rose-500/15 text-rose-400',
    info: 'bg-cyan-500/15 text-cyan-400',
    purple: 'bg-violet-500/15 text-violet-400',
  };
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${variants[variant]}`}>
      {children}
    </span>
  );
}

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  type = 'button',
  onClick,
  disabled,
  className = '',
  ...props
}) {
  const variants = {
    primary: 'bg-cyan-600 hover:bg-cyan-500 text-white shadow-lg shadow-cyan-600/20',
    danger: 'bg-rose-600 hover:bg-rose-500 text-white shadow-lg shadow-rose-600/20',
    success: 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-lg shadow-emerald-600/20',
    ghost: 'bg-slate-700/50 hover:bg-slate-600/70 text-slate-300',
  };
  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-2.5 text-base',
  };
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`inline-flex items-center justify-center gap-2 font-medium rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}

export function Table({ headers, children }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr className="border-b border-slate-700/50">
            {headers.map((h) => (
              <th key={h} className="text-left py-3 px-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-700/30">{children}</tbody>
      </table>
    </div>
  );
}

export function Modal({ open, onClose, title, children, maxWidthClass = 'max-w-lg' }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className={`relative bg-slate-800 border border-slate-700/50 rounded-2xl shadow-2xl w-full ${maxWidthClass} mx-4 max-h-[90vh] overflow-y-auto`}>
        <div className="flex items-center justify-between p-6 border-b border-slate-700/50">
          <h2 className="text-lg font-semibold text-white">{title}</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors text-xl leading-none">&times;</button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

export function Input({ label, ...props }) {
  return (
    <div>
      {label && <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>}
      <input
        className="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all"
        {...props}
      />
    </div>
  );
}

export function TextArea({ label, rows = 4, ...props }) {
  return (
    <div>
      {label && <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>}
      <textarea
        rows={rows}
        className="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all resize-y"
        {...props}
      />
    </div>
  );
}

export function Select({ label, options, ...props }) {
  return (
    <div>
      {label && <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>}
      <select
        className="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all"
        {...props}
      >
        {options.map((o) => (
          <option key={o.value} value={o.value}>{o.label}</option>
        ))}
      </select>
    </div>
  );
}

export function Loader() {
  return (
    <div className="flex items-center justify-center py-12">
      <div className="w-8 h-8 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin" />
    </div>
  );
}

export function EmptyState({ message = 'No data found' }) {
  return (
    <div className="text-center py-12 text-slate-500">
      <p>{message}</p>
    </div>
  );
}
