import React, { useState, useEffect } from 'react';
import { emailApi } from '../services/api';
import { StatusBadge } from '../components/StatusBadge';
import {
  Mail,
  RefreshCw,
  Send,
  CheckCircle,
  AlertCircle,
  Building,
  Calendar,
  Sparkles,
  ExternalLink,
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const EmailEventsPage = () => {
  const [emails, setEmails] = useState([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [showSimulateModal, setShowSimulateModal] = useState(false);

  // Simulation form states
  const [sender, setSender] = useState('recruiting@razorpay.com');
  const [subject, setSubject] = useState('Update on your application for Associate Software Engineer');
  const [body, setBody] = useState('Dear Candidate, your application is currently under review by our tech team.');

  const fetchEmails = async () => {
    setLoading(true);
    try {
      const res = await emailApi.getAll();
      setEmails(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEmails();
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await emailApi.sync();
      await fetchEmails();
    } catch (e) {
      console.error(e);
    } finally {
      setSyncing(false);
    }
  };

  const handleSimulate = async (e) => {
    e.preventDefault();
    try {
      await emailApi.simulate({ sender, subject, body });
      setShowSimulateModal(false);
      await fetchEmails();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Email ATS Event Detection</h1>
          <p className="text-xs text-slate-400">
            Monitors real employer & ATS emails via Gmail OAuth to transition application statuses automatically.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowSimulateModal(true)}
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold shadow-md shadow-purple-600/20 transition"
          >
            <Sparkles className="w-3.5 h-3.5" />
            Simulate Inbound ATS Email
          </button>

          <button
            onClick={handleSync}
            disabled={syncing}
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${syncing ? 'animate-spin' : ''}`} />
            {syncing ? 'Syncing...' : 'Sync Mailbox'}
          </button>
        </div>
      </div>

      {/* Email List */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : emails.length === 0 ? (
        <div className="text-center py-16 glass-panel rounded-2xl border border-slate-800 text-slate-400 space-y-3">
          <Mail className="w-10 h-10 mx-auto text-slate-600" />
          <p>No employer email events detected yet.</p>
          <button
            onClick={() => setShowSimulateModal(true)}
            className="text-xs text-indigo-400 underline font-semibold"
          >
            Simulate a test employer message now
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {emails.map((item) => (
            <div
              key={item.id}
              className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-3"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-indigo-400">{item.sender}</span>
                  {item.detectedStatus && <StatusBadge status={item.detectedStatus} />}
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                    Confidence: {Math.round((item.confidenceScore || 0) * 100)}%
                  </span>
                </div>
                <span className="text-xs text-slate-500">
                  {new Date(item.receivedAt).toLocaleString()}
                </span>
              </div>

              <div>
                <h3 className="text-sm font-bold text-white">{item.subject}</h3>
                <p className="text-xs text-slate-300 mt-1">{item.snippet}</p>
              </div>

              {/* Matched application pill */}
              <div className="flex items-center justify-between pt-2 border-t border-slate-800/80 text-xs">
                {item.applicationId ? (
                  <Link
                    to={`/applications/${item.applicationId}`}
                    className="flex items-center gap-1.5 text-indigo-400 hover:text-indigo-300 font-semibold"
                  >
                    <Building className="w-3.5 h-3.5" />
                    Matched Application: {item.company} – {item.jobTitle} →
                  </Link>
                ) : (
                  <span className="text-slate-400 font-mono text-[11px]">
                    {item.matchRationale || 'No application matched'}
                  </span>
                )}

                <span className="text-[11px] text-emerald-400 flex items-center gap-1">
                  <CheckCircle className="w-3 h-3" />
                  Processed
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Simulation Modal */}
      {showSimulateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fadeIn">
          <div className="glass-panel rounded-2xl p-6 border border-purple-500/30 max-w-lg w-full space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-purple-400" />
                Simulate Inbound Employer Email
              </h3>
              <button
                onClick={() => setShowSimulateModal(false)}
                className="text-slate-400 hover:text-white"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSimulate} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-400 mb-1">Sender Email</label>
                <input
                  type="text"
                  value={sender}
                  onChange={(e) => setSender(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-400 mb-1">Subject</label>
                <input
                  type="text"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-400 mb-1">Email Body Content</label>
                <textarea
                  rows={4}
                  value={body}
                  onChange={(e) => setBody(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowSimulateModal(false)}
                  className="flex-1 py-2.5 rounded-xl border border-slate-700 text-slate-300 text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold shadow-md shadow-purple-600/20"
                >
                  Trigger Event Detection
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
