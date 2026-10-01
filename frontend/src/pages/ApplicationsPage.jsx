import React, { useState, useEffect } from 'react';
import { applicationsApi } from '../services/api';
import { StatusBadge } from '../components/StatusBadge';
import { ScoreGauge } from '../components/ScoreGauge';
import { ManualHandoffModal } from '../components/ManualHandoffModal';
import { Link } from 'react-router-dom';
import {
  FileCheck,
  Building,
  MapPin,
  Calendar,
  AlertTriangle,
  ArrowRight,
  ExternalLink,
  RefreshCw,
  Search,
} from 'lucide-react';

export const ApplicationsPage = () => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedAppForHandoff, setSelectedAppForHandoff] = useState(null);

  const fetchApplications = async () => {
    setLoading(true);
    try {
      const res = await applicationsApi.getAll();
      setApplications(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, []);

  const handleResolveManual = async (appId, resolution) => {
    try {
      await applicationsApi.resolveManual(appId, resolution);
      await fetchApplications();
    } catch (e) {
      console.error(e);
    }
  };

  const filteredApps = applications.filter((app) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      app.company.toLowerCase().includes(term) ||
      app.title.toLowerCase().includes(term) ||
      (app.confirmationId || '').toLowerCase().includes(term);

    if (!matchesSearch) return false;

    if (activeTab === 'ACTION_REQUIRED') {
      return (
        app.status === 'MANUAL_ACTION_REQUIRED' ||
        app.status === 'MANUAL_ELIGIBILITY_REVIEW'
      );
    }
    if (activeTab === 'ACTIVE') {
      return (
        app.status === 'APPLIED' ||
        app.status === 'APPLICATION_RECEIVED' ||
        app.status === 'UNDER_REVIEW' ||
        app.status === 'READY_TO_APPLY'
      );
    }
    if (activeTab === 'INTERVIEWS') {
      return (
        app.status === 'ASSESSMENT_RECEIVED' ||
        app.status === 'INTERVIEW_INVITATION' ||
        app.status === 'INTERVIEW_SCHEDULED' ||
        app.status === 'OFFER_RECEIVED'
      );
    }
    return true;
  });

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Application Tracker</h1>
          <p className="text-xs text-slate-400">
            Real-time status updates, ATS audit trails, and manual action resolution
          </p>
        </div>
        <button
          onClick={fetchApplications}
          className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition"
          title="Refresh"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Tabs */}
      <div className="flex flex-wrap gap-2 border-b border-slate-800 pb-3">
        {[
          { key: 'ALL', label: `All (${applications.length})` },
          {
            key: 'ACTION_REQUIRED',
            label: `⚠️ Action Required (${
              applications.filter(
                (a) =>
                  a.status === 'MANUAL_ACTION_REQUIRED' ||
                  a.status === 'MANUAL_ELIGIBILITY_REVIEW'
              ).length
            })`,
          },
          {
            key: 'ACTIVE',
            label: `Active / Submitted (${
              applications.filter((a) =>
                ['APPLIED', 'APPLICATION_RECEIVED', 'UNDER_REVIEW', 'READY_TO_APPLY'].includes(
                  a.status
                )
              ).length
            })`,
          },
          {
            key: 'INTERVIEWS',
            label: `Interviews & Offers (${
              applications.filter((a) =>
                [
                  'ASSESSMENT_RECEIVED',
                  'INTERVIEW_INVITATION',
                  'INTERVIEW_SCHEDULED',
                  'OFFER_RECEIVED',
                ].includes(a.status)
              ).length
            })`,
          },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
              activeTab === tab.key
                ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                : 'bg-slate-800/80 text-slate-400 hover:text-white'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Search Input */}
      <div className="relative">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
        <input
          type="text"
          placeholder="Filter by company, role title, or confirmation reference ID..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
        />
      </div>

      {/* List */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : filteredApps.length === 0 ? (
        <div className="text-center py-16 glass-panel rounded-2xl border border-slate-800 text-slate-400">
          No applications found in this tab.
        </div>
      ) : (
        <div className="space-y-3">
          {filteredApps.map((app) => (
            <div
              key={app.id}
              className="glass-panel p-5 rounded-2xl border border-slate-800 hover:border-slate-700 transition flex flex-col md:flex-row md:items-center justify-between gap-4"
            >
              <div className="space-y-1.5">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-indigo-400 uppercase tracking-wider">
                    {app.company}
                  </span>
                  <StatusBadge status={app.status} />
                  <ScoreGauge score={app.matchScore} />
                </div>
                <h3 className="text-base font-bold text-white">{app.title}</h3>
                <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400">
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5 text-rose-400" />
                    {app.location}
                  </span>
                  {app.confirmationId && (
                    <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[11px] border border-slate-700">
                      ID: {app.confirmationId}
                    </span>
                  )}
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5" />
                    Updated: {new Date(app.lastStatusChangeAt).toLocaleDateString()}
                  </span>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-2 shrink-0">
                {(app.status === 'MANUAL_ACTION_REQUIRED' ||
                  app.status === 'MANUAL_ELIGIBILITY_REVIEW') && (
                  <button
                    onClick={() => setSelectedAppForHandoff(app)}
                    className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-orange-600 hover:bg-orange-500 text-white text-xs font-semibold shadow-md shadow-orange-600/20 transition"
                  >
                    <AlertTriangle className="w-3.5 h-3.5" />
                    Resolve Action
                  </button>
                )}
                <Link
                  to={`/applications/${app.id}`}
                  className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition"
                >
                  View Details
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Manual Handoff Modal */}
      {selectedAppForHandoff && (
        <ManualHandoffModal
          application={selectedAppForHandoff}
          onClose={() => setSelectedAppForHandoff(null)}
          onResolve={handleResolveManual}
        />
      )}
    </div>
  );
};
