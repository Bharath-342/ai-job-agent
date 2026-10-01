import React, { useState, useEffect } from 'react';
import { settingsApi } from '../services/api';
import {
  Sliders,
  ShieldCheck,
  Zap,
  Save,
  CheckCircle,
  AlertTriangle,
  X,
  Plus
} from 'lucide-react';

export const SettingsPage = () => {
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [newExcluded, setNewExcluded] = useState('');

  const fetchSettings = async () => {
    try {
      const res = await settingsApi.get();
      setSettings(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSuccess(false);
    try {
      const res = await settingsApi.update(settings);
      setSettings(res.data);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const addExcludedRole = () => {
    if (!newExcluded || !newExcluded.trim()) return;
    const current = settings.excludedRoles || [];
    if (!current.includes(newExcluded.trim())) {
      setSettings({ ...settings, excludedRoles: [...current, newExcluded.trim()] });
    }
    setNewExcluded('');
  };

  const removeExcludedRole = (role) => {
    const current = settings.excludedRoles || [];
    setSettings({
      ...settings,
      excludedRoles: current.filter((r) => r !== role),
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSave} className="space-y-6 max-w-3xl mx-auto animate-fadeIn">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Agent Settings & Guardrails</h1>
          <p className="text-xs text-slate-400">
            Configure autonomous application parameters, daily quotas, and minimum match thresholds.
          </p>
        </div>

        <button
          type="submit"
          disabled={saving}
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/25 disabled:opacity-50"
        >
          <Save className="w-4 h-4" />
          {saving ? 'Saving...' : 'Save Settings'}
        </button>
      </div>

      {success && (
        <div className="p-3.5 rounded-xl bg-emerald-950/60 border border-emerald-700 text-emerald-200 text-xs flex items-center gap-2">
          <CheckCircle className="w-4 h-4" />
          Settings updated successfully!
        </div>
      )}

      {/* Quota & Thresholds */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-5">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <Sliders className="w-4 h-4 text-indigo-400" />
          Application Limits & Scoring Thresholds
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 text-xs">
          <div className="space-y-2">
            <label className="block font-semibold text-slate-300">
              Daily Application Limit (Default: 10)
            </label>
            <input
              type="number"
              min={1}
              max={50}
              value={settings?.dailyApplicationLimit || 10}
              onChange={(e) =>
                setSettings({ ...settings, dailyApplicationLimit: parseInt(e.target.value) || 10 })
              }
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
            />
            <p className="text-[11px] text-slate-400">Caps submissions to protect ATS reputation.</p>
          </div>

          <div className="space-y-2">
            <label className="block font-semibold text-slate-300">
              Minimum Match Score Threshold (%)
            </label>
            <input
              type="number"
              min={50}
              max={100}
              value={settings?.minMatchScore || 85}
              onChange={(e) =>
                setSettings({ ...settings, minMatchScore: parseInt(e.target.value) || 85 })
              }
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
            />
            <p className="text-[11px] text-slate-400">Applications scoring below this will not auto-apply.</p>
          </div>
        </div>
      </div>

      {/* Feature Toggles */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <Zap className="w-4 h-4 text-amber-400" />
          Execution Modes & Safety Toggles
        </h3>

        <div className="space-y-3">
          <label className="flex items-center justify-between p-3.5 rounded-xl bg-slate-900/80 border border-slate-800 cursor-pointer">
            <div>
              <span className="font-semibold text-white text-xs block">Mock Mode (Simulated Safety)</span>
              <span className="text-[11px] text-slate-400">
                Simulates submissions without contacting external ATS endpoints. Recommended for testing.
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings?.mockMode ?? true}
              onChange={(e) => setSettings({ ...settings, mockMode: e.target.checked })}
              className="w-4 h-4 text-indigo-600 rounded bg-slate-800 border-slate-700"
            />
          </label>

          <label className="flex items-center justify-between p-3.5 rounded-xl bg-slate-900/80 border border-slate-800 cursor-pointer">
            <div>
              <span className="font-semibold text-white text-xs block">Auto-Apply Allowed</span>
              <span className="text-[11px] text-slate-400">
                Automatically submits applications meeting all criteria without requiring per-job click.
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings?.autoApplyEnabled ?? false}
              onChange={(e) => setSettings({ ...settings, autoApplyEnabled: e.target.checked })}
              className="w-4 h-4 text-indigo-600 rounded bg-slate-800 border-slate-700"
            />
          </label>

          <label className="flex items-center justify-between p-3.5 rounded-xl bg-slate-900/80 border border-slate-800 cursor-pointer">
            <div>
              <span className="font-semibold text-white text-xs block">Live Gmail Monitoring</span>
              <span className="text-[11px] text-slate-400">
                Polls connected Gmail mailbox for real ATS event updates.
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings?.realEmailEnabled ?? false}
              onChange={(e) => setSettings({ ...settings, realEmailEnabled: e.target.checked })}
              className="w-4 h-4 text-indigo-600 rounded bg-slate-800 border-slate-700"
            />
          </label>
        </div>
      </div>

      {/* Excluded Roles Hard Filter */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-rose-400" />
          Excluded Roles (Automatic Hard Rejection)
        </h3>

        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Add excluded keyword (e.g. Senior, Lead, Architect, Principal)..."
            value={newExcluded}
            onChange={(e) => setNewExcluded(e.target.value)}
            className="flex-1 px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
          <button
            type="button"
            onClick={addExcludedRole}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700"
          >
            Add
          </button>
        </div>

        <div className="flex flex-wrap gap-2 pt-1">
          {settings?.excludedRoles?.map((role) => (
            <span
              key={role}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-rose-950/60 text-rose-300 border border-rose-800 text-xs font-medium"
            >
              {role}
              <button
                type="button"
                onClick={() => removeExcludedRole(role)}
                className="hover:text-rose-100"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </span>
          ))}
        </div>
      </div>
    </form>
  );
};
