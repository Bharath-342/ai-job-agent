import React, { useState, useEffect } from 'react';
import { authApi } from '../services/api';
import {
  Mail,
  ShieldCheck,
  CheckCircle,
  ExternalLink,
  AlertTriangle,
  Radio,
  Server,
  Key,
  Layers,
} from 'lucide-react';

export const IntegrationsPage = () => {
  const [googleUrl, setGoogleUrl] = useState('');
  const [connecting, setConnecting] = useState(false);

  useEffect(() => {
    const fetchOAuthUrl = async () => {
      try {
        const res = await authApi.getGoogleUrl();
        setGoogleUrl(res.data.url);
      } catch (e) {
        console.error(e);
      }
    };
    fetchOAuthUrl();
  }, []);

  const handleConnectGoogle = () => {
    setConnecting(true);
    if (googleUrl) {
      window.location.href = googleUrl;
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto animate-fadeIn">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight">External Integrations & ATS Connectors</h1>
        <p className="text-xs text-slate-400">
          Secure OAuth credentials and verified ATS connections. We enforce least-privilege security and zero scraping bans.
        </p>
      </div>

      {/* Gmail OAuth Card */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20">
              <Mail className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white">Google Gmail OAuth Integration</h3>
                <span className="px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-800 text-[10px] font-semibold">
                  Connected (Active)
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Monitors candidate inbox strictly for ATS status updates and recruiter emails.
              </p>
            </div>
          </div>

          <button
            onClick={handleConnectGoogle}
            className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition"
          >
            <Key className="w-4 h-4 text-amber-400" />
            Reconnect Google OAuth
          </button>
        </div>

        <div className="p-4 rounded-xl bg-slate-900/80 border border-slate-800 text-xs text-slate-400 space-y-1.5">
          <div className="font-bold text-slate-200 flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            Security & Least Privilege Architecture:
          </div>
          <div>• Requests only <code className="text-indigo-400 font-mono">gmail.readonly</code> scope (cannot send or delete emails).</div>
          <div>• Passwords are never requested or stored. OAuth access tokens are securely managed.</div>
          <div>• Unrelated newsletters, marketing, and spam are permanently filtered out.</div>
        </div>
      </div>

      {/* Supported ATS Connectors */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <Layers className="w-4 h-4 text-indigo-400" />
          Verified Application Integrations (V1 Scope)
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm">Greenhouse ATS</span>
              <span className="px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-800 text-[10px]">
                Supported
              </span>
            </div>
            <p className="text-slate-400">
              Verified job ingestion and automated multi-field form mapping for companies utilizing Greenhouse boards.
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm">Lever ATS</span>
              <span className="px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-800 text-[10px]">
                Supported
              </span>
            </div>
            <p className="text-slate-400">
              Direct application payload integration for fast-growing Indian tech startups hosting postings on Lever.
            </p>
          </div>
        </div>
      </div>

      {/* Explicit Unsupported Sites & Guardrails */}
      <div className="glass-panel p-6 rounded-3xl border border-orange-500/20 bg-orange-950/5 space-y-3">
        <h3 className="text-sm font-bold text-orange-300 uppercase tracking-wider flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-orange-400" />
          Unsupported Platforms & Safe Human Handoff Policy
        </h3>
        <p className="text-xs text-slate-300 leading-relaxed">
          In strict compliance with Terms of Service and anti-bot regulations, this agent does NOT attempt universal unauthorized automation on:
        </p>

        <div className="flex flex-wrap gap-2 text-xs">
          {['LinkedIn', 'Naukri.com', 'Indeed', 'Workday', 'TCS iON', 'Unverified Portals'].map((site) => (
            <span
              key={site}
              className="px-3 py-1 rounded-xl bg-slate-900 text-slate-300 border border-slate-800 font-mono text-[11px]"
            >
              ⛔ {site} (Manual Handoff Triggered)
            </span>
          ))}
        </div>

        <p className="text-xs text-slate-400">
          When jobs from these platforms are discovered, the system prepares candidate details, halts automated execution, and prompts a 1-click human handoff modal.
        </p>
      </div>
    </div>
  );
};
