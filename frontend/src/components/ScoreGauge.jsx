import React from 'react';

export const ScoreGauge = ({ score }) => {
  const getScoreColor = (val) => {
    if (val >= 85) return 'text-emerald-400 border-emerald-500/40 bg-emerald-950/30';
    if (val >= 70) return 'text-amber-400 border-amber-500/40 bg-amber-950/30';
    return 'text-rose-400 border-rose-500/40 bg-rose-950/30';
  };

  return (
    <div className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg border font-bold text-sm ${getScoreColor(score)}`}>
      <span className="text-xs uppercase tracking-wider text-slate-400 font-medium">Match</span>
      <span>{score}%</span>
    </div>
  );
};
