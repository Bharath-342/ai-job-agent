import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Briefcase,
  FileCheck,
  FileText,
  UserCheck,
  Mail,
  Bell,
  Sliders,
  Radio,
} from 'lucide-react';

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/jobs', label: 'India Jobs (2026)', icon: Briefcase },
  { to: '/applications', label: 'Applications', icon: FileCheck },
  { to: '/resume', label: 'Resume Parser', icon: FileText },
  { to: '/profile', label: 'Candidate Profile', icon: UserCheck },
  { to: '/emails', label: 'Email Events (ATS)', icon: Mail },
  { to: '/notifications', label: 'Notifications', icon: Bell },
  { to: '/integrations', label: 'Integrations', icon: Radio },
  { to: '/settings', label: 'Settings & Quota', icon: Sliders },
];

export const Sidebar = () => {
  return (
    <aside className="w-64 glass-panel border-r border-slate-800/80 min-h-[calc(100vh-61px)] p-4 flex flex-col justify-between">
      <div className="space-y-1">
        <div className="px-3 py-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
          Navigation
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition ${
                  isActive
                    ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/25'
                    : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </div>

      {/* Target Guardrail Notice */}
      <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 text-xs text-slate-400 space-y-1.5">
        <div className="font-semibold text-slate-200 flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
          Target Active
        </div>
        <div className="text-[11px] leading-relaxed">
          🎓 <strong className="text-white">2026 Batch</strong><br />
          💼 <strong className="text-white">0-1 yrs Fresher</strong><br />
          🇮🇳 <strong className="text-white">India Only</strong> (Foreign rejected)
        </div>
      </div>
    </aside>
  );
};
