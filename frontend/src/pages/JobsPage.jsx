import React, { useState, useEffect } from 'react';
import { jobsApi, applicationsApi } from '../services/api';
import { ScoreGauge } from '../components/ScoreGauge';
import { StatusBadge } from '../components/StatusBadge';
import { ManualHandoffModal } from '../components/ManualHandoffModal';
import {
  Search,
  Filter,
  ExternalLink,
  Send,
  AlertTriangle,
  CheckCircle,
  MapPin,
  Calendar,
  Building,
  RefreshCw,
  Info
} from 'lucide-react';

export const JobsPage = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterEligible, setFilterEligible] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedAppForHandoff, setSelectedAppForHandoff] = useState(null);
  const [applyingJobId, setApplyingJobId] = useState(null);
  const [statusMessage, setStatusMessage] = useState(null);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      if (filterEligible) {
        const res = await jobsApi.getEligible();
        setJobs(res.data);
      } else {
        const res = await jobsApi.getAll();
        setJobs(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, [filterEligible]);

  const handleApply = async (jobId) => {
    setApplyingJobId(jobId);
    setStatusMessage(null);
    try {
      const res = await applicationsApi.apply({ jobId });
      const app = res.data;

      if (app.status === 'MANUAL_ACTION_REQUIRED' || app.status === 'MANUAL_ELIGIBILITY_REVIEW') {
        setSelectedAppForHandoff(app);
      } else {
        setStatusMessage({
          type: 'success',
          text: `Application submitted successfully for ${app.title}! Confirmation: ${app.confirmationId || 'PENDING'}`,
        });
        await fetchJobs();
      }
    } catch (err) {
      setStatusMessage({
        type: 'error',
        text: err.response?.data?.message || 'Application submission halted. Check requirements.',
      });
    } finally {
      setApplyingJobId(null);
    }
  };

  const handleResolveManual = async (appId, resolution) => {
    try {
      await applicationsApi.resolveManual(appId, resolution);
      setStatusMessage({
        type: 'success',
        text: 'Manual handoff resolved! Application marked as submitted.',
      });
      await fetchJobs();
    } catch (e) {
      console.error(e);
    }
  };

  const filteredList = jobs.filter((j) => {
    const term = searchTerm.toLowerCase();
    const company = (j.company || '').toLowerCase();
    const title = (j.title || '').toLowerCase();
    const loc = (j.location || '').toLowerCase();
    return company.includes(term) || title.includes(term) || loc.includes(term);
  });

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">India Job Discovery</h1>
          <p className="text-xs text-slate-400">
            Strict 2026 fresher & India-only guardrails active. Foreign & experienced roles rejected.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setFilterEligible(!filterEligible)}
            className={`px-3.5 py-2 rounded-xl text-xs font-semibold transition border ${
              filterEligible
                ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40 shadow-sm'
                : 'bg-slate-800 text-slate-300 border-slate-700'
            }`}
          >
            {filterEligible ? 'Showing: 2026 Eligible Only' : 'Showing: All Discovered'}
          </button>
          <button
            onClick={fetchJobs}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition"
            title="Refresh"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Status Alert Banner */}
      {statusMessage && (
        <div
          className={`p-4 rounded-xl text-sm border flex items-center justify-between ${
            statusMessage.type === 'success'
              ? 'bg-emerald-950/60 text-emerald-200 border-emerald-700'
              : 'bg-rose-950/60 text-rose-200 border-rose-700'
          }`}
        >
          <span>{statusMessage.text}</span>
          <button onClick={() => setStatusMessage(null)} className="text-xs font-bold underline ml-4">
            Dismiss
          </button>
        </div>
      )}

      {/* Search Bar */}
      <div className="relative">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
        <input
          type="text"
          placeholder="Search by company, role (e.g. Java Backend), or location (Hyderabad, Bangalore, Pune)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-900/80 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
        />
      </div>

      {/* Jobs Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : filteredList.length === 0 ? (
        <div className="text-center py-16 glass-panel rounded-2xl border border-slate-800 text-slate-400">
          No jobs found matching your filter criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredList.map((job) => {
            const isMatchObject = job.matchScore !== undefined;
            const score = isMatchObject ? job.matchScore : 85;
            const jobId = isMatchObject ? job.jobId : job.id;
            const eligible = isMatchObject ? job.eligible : true;
            const manualReview = isMatchObject ? job.manualReviewRequired : false;

            return (
              <div
                key={job.jobKey || jobId}
                className="glass-panel p-5 rounded-2xl border border-slate-800 hover:border-slate-700 transition flex flex-col justify-between space-y-4"
              >
                <div className="space-y-3">
                  {/* Top Bar: Company & Score */}
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <span className="text-xs font-bold uppercase tracking-wider text-indigo-400 flex items-center gap-1.5">
                        <Building className="w-3.5 h-3.5" />
                        {job.company}
                      </span>
                      <h3 className="text-lg font-bold text-white mt-1 leading-snug">{job.title}</h3>
                    </div>
                    {isMatchObject && <ScoreGauge score={score} />}
                  </div>

                  {/* Location & Tags */}
                  <div className="flex flex-wrap items-center gap-2 text-xs text-slate-400">
                    <span className="flex items-center gap-1 text-slate-300">
                      <MapPin className="w-3.5 h-3.5 text-rose-400" />
                      {job.location}
                    </span>
                    <span className="px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                      🎓 2026 Batch
                    </span>
                    <span className="px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                      💼 Fresher / 0-1 yrs
                    </span>
                    {manualReview && (
                      <span className="px-2 py-0.5 rounded-full bg-amber-950 text-amber-300 border border-amber-800">
                        ⚠️ Review Required
                      </span>
                    )}
                  </div>

                  {/* Skills overview */}
                  {job.matchedSkills && job.matchedSkills.length > 0 && (
                    <div className="space-y-1">
                      <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                        Matched Skills:
                      </span>
                      <div className="flex flex-wrap gap-1.5">
                        {job.matchedSkills.map((s) => (
                          <span
                            key={s}
                            className="px-2 py-0.5 rounded-md bg-emerald-950/60 text-emerald-300 border border-emerald-800/60 text-[11px]"
                          >
                            ✓ {s}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {job.missingSkills && job.missingSkills.length > 0 && (
                    <div className="space-y-1">
                      <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                        Missing Skills:
                      </span>
                      <div className="flex flex-wrap gap-1.5">
                        {job.missingSkills.map((s) => (
                          <span
                            key={s}
                            className="px-2 py-0.5 rounded-md bg-slate-800 text-slate-400 text-[11px]"
                          >
                            • {s}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {job.eligibilityReason && (
                    <p className="text-xs text-slate-400 bg-slate-900/60 p-2.5 rounded-xl border border-slate-800/80">
                      ℹ️ {job.eligibilityReason}
                    </p>
                  )}
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2 pt-3 border-t border-slate-800/80">
                  <a
                    href={job.jobUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="p-2.5 rounded-xl border border-slate-700 hover:bg-slate-800 text-slate-300 transition"
                    title="Open Employer Career Portal"
                  >
                    <ExternalLink className="w-4 h-4" />
                  </a>

                  <button
                    onClick={() => handleApply(jobId)}
                    disabled={applyingJobId === jobId || (!eligible && !manualReview)}
                    className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-500 text-white font-medium text-xs transition shadow-lg shadow-indigo-600/20"
                  >
                    <Send className="w-3.5 h-3.5" />
                    {applyingJobId === jobId
                      ? 'Processing...'
                      : manualReview
                      ? 'Review & Apply'
                      : 'Apply / Auto-Apply'}
                  </button>
                </div>
              </div>
            );
          })}
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
