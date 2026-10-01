import React, { useState, useEffect } from 'react';
import { Bell, ShieldCheck, LogOut, User, RefreshCw, Zap } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { notificationsApi, dashboardApi } from '../services/api';
import { useNavigate } from 'react-router-dom';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);
  const [quotaInfo, setQuotaInfo] = useState({ today: 0, limit: 10, remaining: 10 });

  const loadData = async () => {
    try {
      const [notifRes, dashRes] = await Promise.all([
        notificationsApi.getUnreadCount(),
        dashboardApi.getStats()
      ]);
      setUnreadCount(notifRes.data.count || 0);
      setQuotaInfo({
        today: dashRes.data.applicationsToday || 0,
        limit: dashRes.data.dailyLimit || 10,
        remaining: dashRes.data.remainingQuota || 10,
      });
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-40 w-full glass-panel border-b border-slate-800/80 px-6 py-3.5 flex items-center justify-between">
      {/* Brand & Badge */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-purple-500 flex items-center justify-center shadow-lg shadow-indigo-500/25">
            <Zap className="w-5 h-5 text-white" />
          </div>
          <div>
            <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-indigo-200 bg-clip-text text-transparent">
              AI Job Agent
            </span>
            <span className="block text-[10px] font-semibold tracking-wider text-indigo-400 uppercase">
              2026 Fresher Edition • India IT
            </span>
          </div>
        </div>

        <div className="hidden md:flex items-center gap-2 ml-4">
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-500/10 text-amber-300 border border-amber-500/20">
            <ShieldCheck className="w-3.5 h-3.5 text-amber-400" />
            MOCK MODE (Safe)
          </span>
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
            Quota: {quotaInfo.today}/{quotaInfo.limit} today ({quotaInfo.remaining} left)
          </span>
        </div>
      </div>

      {/* Right Action Icons & User */}
      <div className="flex items-center gap-4">
        {/* Notifications Bell */}
        <button
          onClick={() => navigate('/notifications')}
          className="relative p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          title="Notifications"
        >
          <Bell className="w-5 h-5" />
          {unreadCount > 0 && (
            <span className="absolute top-1 right-1 flex h-4 w-4 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white shadow-sm">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </button>

        {/* User Profile & Logout */}
        <div className="flex items-center gap-3 pl-3 border-l border-slate-800">
          <div className="text-right hidden sm:block">
            <div className="text-sm font-semibold text-white">{user?.fullName || 'Candidate'}</div>
            <div className="text-xs text-slate-400">{user?.email}</div>
          </div>
          <button
            onClick={logout}
            className="p-2 rounded-xl text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition"
            title="Sign Out"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </div>
    </header>
  );
};
