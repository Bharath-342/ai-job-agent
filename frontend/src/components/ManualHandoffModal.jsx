import React, { useState } from 'react';
import { ExternalLink, CheckCircle, AlertTriangle, X } from 'lucide-react';
import { StatusBadge } from './StatusBadge';
import { ScoreGauge } from './ScoreGauge';

export const ManualHandoffModal = ({ application, onClose, onResolve }) => {
  const [confirmationId, setConfirmationId] = useState('');
  const [userNotes, setUserNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!application) return null;

  const handleMarkApplied = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await onResolve(application.id, {
        resolutionStatus: 'APPLIED',
        confirmationId: confirmationId || `CONF-${Date.now().toString().slice(-6)}`,
        userNotes,
      });
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-lg glass-panel rounded-2xl p-6 border border-orange-500/40 shadow-2xl shadow-orange-500/10">
        
        {/* Header */}
        <div className="flex items-start justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-orange-500/20 text-orange-400 border border-orange-500/30">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-xl font-bold text-white">Manual Action Required</h3>
              <p className="text-xs text-orange-400 font-medium">System halted automated submission for human review</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Job Details Card */}
        <div className="my-5 p-4 rounded-xl bg-slate-900/90 border border-slate-800 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-indigo-400">{application.company}</span>
            <ScoreGauge score={application.matchScore || 90} />
          </div>
          <h4 className="text-lg font-bold text-white">{application.title}</h4>
          <p className="text-sm text-slate-400 flex items-center gap-2">
            📍 {application.location} • <StatusBadge status={application.status} />
          </p>

          <div className="p-3 rounded-lg bg-orange-950/40 border border-orange-900/60 text-xs text-orange-200">
            <strong className="block font-semibold mb-1">Reason for Human Handoff:</strong>
            {application.manualActionReason || application.eligibilityReason || 'Unknown mandatory application question encountered. Zero-hallucination guardrail active.'}
          </div>
        </div>

        {/* Step 1: Open Portal */}
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">
              Step 1: Open Employer Application Portal
            </label>
            <a
              href={application.jobUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-medium transition shadow-lg shadow-indigo-600/20"
            >
              <ExternalLink className="w-4 h-4" />
              Open Application Page
            </a>
          </div>

          {/* Step 2: Mark Applied */}
          <form onSubmit={handleMarkApplied} className="space-y-3 pt-3 border-t border-slate-800">
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
              Step 2: Confirm Submission
            </label>

            <div>
              <input
                type="text"
                placeholder="Confirmation ID / Reference # (optional)"
                value={confirmationId}
                onChange={(e) => setConfirmationId(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-lg bg-slate-900 border border-slate-700 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <input
                type="text"
                placeholder="Add personal notes (e.g. sponsorship answered No)"
                value={userNotes}
                onChange={(e) => setUserNotes(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-lg bg-slate-900 border border-slate-700 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 px-4 py-2.5 rounded-xl border border-slate-700 text-slate-300 hover:bg-slate-800 text-sm font-medium transition"
              >
                Decide Later
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-semibold transition shadow-lg shadow-emerald-600/20 disabled:opacity-50"
              >
                <CheckCircle className="w-4 h-4" />
                {submitting ? 'Updating...' : 'Mark as Applied'}
              </button>
            </div>
          </form>
        </div>

      </div>
    </div>
  );
};
