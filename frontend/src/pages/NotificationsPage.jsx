import React, { useState, useEffect } from 'react';
import { notificationsApi } from '../services/api';
import { Bell, CheckCheck, Clock, ExternalLink, AlertTriangle, Send, FileCheck } from 'lucide-react';
import { Link } from 'react-router-dom';

export const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationsApi.getAll();
      setNotifications(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkAllRead = async () => {
    try {
      await notificationsApi.markAllRead();
      await fetchNotifications();
    } catch (e) {
      console.error(e);
    }
  };

  const handleMarkRead = async (id) => {
    try {
      await notificationsApi.markRead(id);
      await fetchNotifications();
    } catch (e) {
      console.error(e);
    }
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'MANUAL_ACTION_REQUIRED':
        return <AlertTriangle className="w-5 h-5 text-orange-400" />;
      case 'APPLICATION_SUBMITTED':
        return <Send className="w-5 h-5 text-indigo-400" />;
      default:
        return <Bell className="w-5 h-5 text-emerald-400" />;
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto animate-fadeIn">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Notification Feed</h1>
          <p className="text-xs text-slate-400">
            Real-time notifications for submitted applications, manual action alerts, and employer responses.
          </p>
        </div>

        <button
          onClick={handleMarkAllRead}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold border border-slate-700 transition"
        >
          <CheckCheck className="w-4 h-4" />
          Mark All as Read
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : notifications.length === 0 ? (
        <div className="text-center py-16 glass-panel rounded-2xl border border-slate-800 text-slate-400">
          No notifications yet.
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map((n) => (
            <div
              key={n.id}
              className={`glass-panel p-4 rounded-2xl border transition flex items-start gap-4 ${
                n.isRead ? 'border-slate-800/80 opacity-75' : 'border-indigo-500/40 bg-indigo-950/10'
              }`}
            >
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 shrink-0">
                {getNotificationIcon(n.type)}
              </div>

              <div className="flex-1 space-y-1">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-bold text-white">{n.title}</h3>
                  <span className="text-[11px] text-slate-500">
                    {new Date(n.createdAt).toLocaleString()}
                  </span>
                </div>
                <p className="text-xs text-slate-300">{n.message}</p>

                <div className="flex items-center gap-3 pt-1">
                  {n.linkUrl && (
                    <Link
                      to={n.linkUrl}
                      className="text-xs text-indigo-400 hover:text-indigo-300 font-semibold flex items-center gap-1"
                    >
                      View Application Details →
                    </Link>
                  )}
                  {!n.isRead && (
                    <button
                      onClick={() => handleMarkRead(n.id)}
                      className="text-[11px] text-slate-400 hover:text-white underline"
                    >
                      Mark read
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
