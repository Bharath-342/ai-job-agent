import React, { useState, useEffect } from 'react';
import { dashboardApi, jobsApi } from '../services/api';
import {
  Briefcase,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Award,
  Calendar,
  Send,
  Zap,
  TrendingUp,
  ArrowRight,
  RefreshCw
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../components/StatusBadge';

export const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [discovering, setDiscovering] = useState(false);

  const fetchStats = async () => {
    try {
      const res = await dashboardApi.getStats();
      setStats(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const handleRunDiscovery = async () => {
    setDiscovering(true);
    try {
      await jobsApi.discover();
      await fetchStats();
    } catch (e) {
      console.error(e);
    } finally {
      setDiscovering(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  const quotaPercent = Math.min(100, Math.round(((stats?.applicationsToday || 0) / (stats?.dailyLimit || 10)) * 100));

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Top Banner / Hero */}
      <div className="relative overflow-hidden rounded-3xl glass-panel p-8 border border-indigo-500/20 bg-gradient-to-r from-slate-900/90 via-indigo-950/30 to-purple-950/20">
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
              <Zap className="w-3.5 h-3.5 text-indigo-400" />
              Autonomous + Human-In-The-Loop Agent Active
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
              2026 Fresher Career Hub
            </h1>
            <p className="text-sm sm:text-base text-slate-300 max-w-2xl">
              Strict India-only guardrails, multi-dimensional skill matching, zero-hallucination form handoffs, and real Gmail employer event detection.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleRunDiscovery}
              disabled={discovering}
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-medium transition shadow-lg shadow-indigo-600/25 disabled:opacity-50 text-sm"
            >
              <RefreshCw className={`w-4 h-4 ${discovering ? 'animate-spin' : ''}`} />
              {discovering ? 'Scanning ATS...' : 'Discover Jobs'}
            </button>
            <Link
              to="/jobs"
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-medium transition text-sm border border-slate-700"
            >
              View Eligible
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </div>

      {/* Daily Quota Card */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800">
        <div className="flex items-center justify-between mb-3">
          <div>
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Daily Submission Quota</h3>
            <p className="text-xs text-slate-400">Protects candidate reputation & avoids anti-bot flag</p>
          </div>
          <span className="text-sm font-extrabold text-indigo-400">
            {stats?.applicationsToday || 0} / {stats?.dailyLimit || 10} Used ({stats?.remainingQuota || 10} Remaining)
          </span>
        </div>
        <div className="w-full h-3 rounded-full bg-slate-800 overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-indigo-500 to-emerald-400 transition-all duration-500 rounded-full"
            style={{ width: `${quotaPercent}%` }}
          />
        </div>
      </div>

      {/* Stat Cards Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Discovered */}
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Discovered</span>
            <Briefcase className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-extrabold text-white">{stats?.jobsDiscovered || 0}</div>
          <div className="text-xs text-slate-400">Across verified ATS platforms</div>
        </div>

        {/* 2026 Eligible */}
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Eligible (2026)</span>
            <TrendingUp className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-extrabold text-emerald-400">{stats?.eligibleJobs || 0}</div>
          <div className="text-xs text-emerald-400/80">India • Fresher / 0-1 yrs</div>
        </div>

        {/* Submitted */}
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Submitted</span>
            <Send className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-extrabold text-indigo-400">{stats?.applicationsSubmitted || 0}</div>
          <div className="text-xs text-slate-400">With verified confirmations</div>
        </div>

        {/* Actions Required */}
        <div className="glass-panel p-5 rounded-2xl border border-orange-500/30 bg-orange-950/10 space-y-2">
          <div className="flex items-center justify-between text-orange-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Action Needed</span>
            <AlertTriangle className="w-4 h-4 text-orange-400" />
          </div>
          <div className="text-2xl font-extrabold text-orange-300">{stats?.manualActionsRequired || 0}</div>
          <div className="text-xs text-orange-400/80">Human review / form handoff</div>
        </div>
      </div>

      {/* Funnel Metrics Grid */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
        <div className="glass-panel p-4 rounded-xl border border-slate-800 text-center space-y-1">
          <div className="text-xs font-semibold uppercase text-amber-400">Under Review</div>
          <div className="text-xl font-bold text-white">{stats?.underReview || 0}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl border border-slate-800 text-center space-y-1">
          <div className="text-xs font-semibold uppercase text-cyan-400">Assessments</div>
          <div className="text-xl font-bold text-white">{stats?.assessments || 0}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl border border-slate-800 text-center space-y-1">
          <div className="text-xs font-semibold uppercase text-sky-400">Interviews</div>
          <div className="text-xl font-bold text-white">{stats?.interviews || 0}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl border border-slate-800 text-center space-y-1">
          <div className="text-xs font-semibold uppercase text-rose-400">Rejections</div>
          <div className="text-xl font-bold text-white">{stats?.rejections || 0}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl border border-emerald-500/30 bg-emerald-950/20 text-center space-y-1 col-span-2 md:col-span-1">
          <div className="text-xs font-semibold uppercase text-emerald-400">Offers 🎉</div>
          <div className="text-xl font-bold text-emerald-300">{stats?.offers || 0}</div>
        </div>
      </div>

      {/* Recent Activity Timeline */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-indigo-400" />
            <h3 className="text-base font-bold text-white">Live Application Timeline</h3>
          </div>
          <Link to="/applications" className="text-xs text-indigo-400 hover:text-indigo-300 font-medium">
            View All Applications →
          </Link>
        </div>

        {stats?.recentEvents && stats.recentEvents.length > 0 ? (
          <div className="space-y-3">
            {stats.recentEvents.map((event) => (
              <div
                key={event.id}
                className="flex items-start justify-between p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 text-sm"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-white">{event.eventType}</span>
                    <StatusBadge status={event.newStatus} />
                    <span className="text-[10px] px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                      {event.source}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400">{event.description}</p>
                </div>
                <span className="text-xs text-slate-400 shrink-0">
                  {new Date(event.eventTime).toLocaleDateString()}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8 text-slate-400 text-sm">
            No events recorded yet. Run job discovery and initiate your first application!
          </div>
        )}
      </div>
    </div>
  );
};
