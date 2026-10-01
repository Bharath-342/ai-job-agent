import React from 'react';

const statusConfig = {
  DISCOVERED: { label: 'Discovered', bg: 'bg-slate-800 text-slate-300 border-slate-700' },
  MATCHED: { label: 'Matched', bg: 'bg-blue-900/60 text-blue-300 border-blue-700/60' },
  READY_TO_APPLY: { label: 'Ready to Apply', bg: 'bg-indigo-900/60 text-indigo-300 border-indigo-700/60' },
  APPLYING: { label: 'Applying...', bg: 'bg-purple-900/60 text-purple-300 border-purple-700/60 animate-pulse' },
  APPLIED: { label: 'Applied', bg: 'bg-teal-900/60 text-teal-300 border-teal-700/60' },
  APPLICATION_RECEIVED: { label: 'Received by Employer', bg: 'bg-emerald-900/60 text-emerald-300 border-emerald-700/60' },
  UNDER_REVIEW: { label: 'Under Review', bg: 'bg-amber-900/60 text-amber-300 border-amber-700/60' },
  ASSESSMENT_RECEIVED: { label: 'Assessment Received', bg: 'bg-cyan-900/60 text-cyan-300 border-cyan-700/60' },
  INTERVIEW_INVITATION: { label: 'Interview Invitation', bg: 'bg-sky-900/60 text-sky-300 border-sky-700/60 font-semibold' },
  INTERVIEW_SCHEDULED: { label: 'Interview Scheduled', bg: 'bg-blue-800/80 text-blue-200 border-blue-600 font-semibold' },
  OFFER_RECEIVED: { label: 'Offer Received 🎉', bg: 'bg-emerald-700 text-emerald-100 border-emerald-500 font-bold' },
  REJECTED_BY_COMPANY: { label: 'Rejected by Company', bg: 'bg-rose-950 text-rose-300 border-rose-800/60' },
  REJECTED: { label: 'Ineligible / Rejected', bg: 'bg-rose-950/80 text-rose-400 border-rose-900/50' },
  MANUAL_ELIGIBILITY_REVIEW: { label: 'Eligibility Review', bg: 'bg-amber-950 text-amber-400 border-amber-800' },
  MANUAL_ACTION_REQUIRED: { label: 'Action Required ⚠️', bg: 'bg-orange-950 text-orange-300 border-orange-700 font-semibold animate-pulse' },
  APPLICATION_FAILED: { label: 'Failed', bg: 'bg-red-950 text-red-400 border-red-800' },
  WITHDRAWN: { label: 'Withdrawn', bg: 'bg-slate-800 text-slate-400 border-slate-700' },
};

export const StatusBadge = ({ status }) => {
  const config = statusConfig[status] || { label: status || 'Unknown', bg: 'bg-slate-800 text-slate-300 border-slate-700' };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs border ${config.bg}`}>
      {config.label}
    </span>
  );
};
