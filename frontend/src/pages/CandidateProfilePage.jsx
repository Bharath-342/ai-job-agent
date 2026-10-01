import React, { useState, useEffect } from 'react';
import { profileApi } from '../services/api';
import {
  User,
  Mail,
  Phone,
  GraduationCap,
  Briefcase,
  MapPin,
  Code,
  Link as LinkIcon,
  Save,
  Plus,
  X,
  CheckCircle,
  FileText
} from 'lucide-react';

export const CandidateProfilePage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // New tag inputs
  const [newSkill, setNewSkill] = useState('');
  const [newRole, setNewRole] = useState('');
  const [newLoc, setNewLoc] = useState('');

  const fetchProfile = async () => {
    try {
      const res = await profileApi.get();
      setProfile(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSaveSuccess(false);
    try {
      const res = await profileApi.update(profile);
      setProfile(res.data);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 4000);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const addTag = (field, value, setter) => {
    if (!value || !value.trim()) return;
    const current = profile[field] || [];
    if (!current.includes(value.trim())) {
      setProfile({ ...profile, [field]: [...current, value.trim()] });
    }
    setter('');
  };

  const removeTag = (field, valToRemove) => {
    const current = profile[field] || [];
    setProfile({
      ...profile,
      [field]: current.filter((item) => item !== valToRemove),
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
    <form onSubmit={handleSave} className="space-y-6 max-w-4xl mx-auto animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Candidate Profile</h1>
          <p className="text-xs text-slate-400">
            Review and edit your profile extracted from resume. This is the source of truth for job matching.
          </p>
        </div>

        <button
          type="submit"
          disabled={saving}
          className="flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/25 disabled:opacity-50"
        >
          <Save className="w-4 h-4" />
          {saving ? 'Saving Changes...' : 'Save Profile'}
        </button>
      </div>

      {saveSuccess && (
        <div className="p-3.5 rounded-xl bg-emerald-950/60 border border-emerald-700 text-emerald-200 text-xs flex items-center gap-2">
          <CheckCircle className="w-4 h-4" />
          Candidate profile updated successfully!
        </div>
      )}

      {/* Active Resume Notification */}
      {profile?.activeResumeFilename && (
        <div className="flex items-center justify-between p-3.5 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-300">
          <span className="flex items-center gap-2">
            <FileText className="w-4 h-4 text-indigo-400" />
            Active Source Resume: <strong className="text-white">{profile.activeResumeFilename}</strong>
          </span>
          <span className="text-[11px] text-slate-400">
            Last Updated: {profile.updatedAt ? new Date(profile.updatedAt).toLocaleDateString() : 'Recent'}
          </span>
        </div>
      )}

      {/* Section 1: Basic Information */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <User className="w-4 h-4 text-indigo-400" />
          Personal & Contact Details
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          <div>
            <label className="block font-semibold text-slate-400 mb-1">Full Name</label>
            <input
              type="text"
              value={profile?.fullName || ''}
              onChange={(e) => setProfile({ ...profile, fullName: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
              required
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-400 mb-1">Email</label>
            <input
              type="email"
              value={profile?.email || ''}
              onChange={(e) => setProfile({ ...profile, email: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
              required
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-400 mb-1">Phone</label>
            <input
              type="text"
              value={profile?.phone || ''}
              onChange={(e) => setProfile({ ...profile, phone: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
            />
          </div>
        </div>
      </div>

      {/* Section 2: Education & Graduation */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <GraduationCap className="w-4 h-4 text-emerald-400" />
          Education & 2026 Batch Guardrail
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          <div>
            <label className="block font-semibold text-slate-400 mb-1">Degree</label>
            <input
              type="text"
              value={profile?.degree || ''}
              onChange={(e) => setProfile({ ...profile, degree: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-400 mb-1">Graduation Year (Batch)</label>
            <input
              type="number"
              value={profile?.graduationYear || 2026}
              onChange={(e) => setProfile({ ...profile, graduationYear: parseInt(e.target.value) || 2026 })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500 font-bold text-indigo-400"
              required
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-400 mb-1">Experience Years (Fresher: 0)</label>
            <input
              type="number"
              step="0.1"
              value={profile?.experienceYears || 0.0}
              onChange={(e) => setProfile({ ...profile, experienceYears: parseFloat(e.target.value) || 0.0 })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white focus:outline-none focus:border-indigo-500"
            />
          </div>
        </div>
      </div>

      {/* Section 3: Skills Tags */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <Code className="w-4 h-4 text-blue-400" />
          Skills & Technical Expertise
        </h3>

        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Add skill (e.g. Java, Spring Boot, PostgreSQL, Docker)..."
            value={newSkill}
            onChange={(e) => setNewSkill(e.target.value)}
            className="flex-1 px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
          <button
            type="button"
            onClick={() => addTag('skills', newSkill, setNewSkill)}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700"
          >
            Add
          </button>
        </div>

        <div className="flex flex-wrap gap-2 pt-2">
          {profile?.skills?.map((s) => (
            <span
              key={s}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-indigo-950/60 text-indigo-300 border border-indigo-800 text-xs font-medium"
            >
              {s}
              <button
                type="button"
                onClick={() => removeTag('skills', s)}
                className="hover:text-rose-400"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </span>
          ))}
        </div>
      </div>

      {/* Section 4: Target Roles */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <Briefcase className="w-4 h-4 text-purple-400" />
          Target Roles
        </h3>

        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Add target role (e.g. Java Backend Developer)..."
            value={newRole}
            onChange={(e) => setNewRole(e.target.value)}
            className="flex-1 px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
          <button
            type="button"
            onClick={() => addTag('targetRoles', newRole, setNewRole)}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700"
          >
            Add
          </button>
        </div>

        <div className="flex flex-wrap gap-2 pt-2">
          {profile?.targetRoles?.map((r) => (
            <span
              key={r}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-purple-950/60 text-purple-300 border border-purple-800 text-xs font-medium"
            >
              {r}
              <button
                type="button"
                onClick={() => removeTag('targetRoles', r)}
                className="hover:text-rose-400"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </span>
          ))}
        </div>
      </div>

      {/* Section 5: Preferred Indian Locations */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <MapPin className="w-4 h-4 text-rose-400" />
          Preferred Locations in India
        </h3>

        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Add Indian location (e.g. Hyderabad, Bangalore, Pune, Remote - India)..."
            value={newLoc}
            onChange={(e) => setNewLoc(e.target.value)}
            className="flex-1 px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
          <button
            type="button"
            onClick={() => addTag('preferredLocations', newLoc, setNewLoc)}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700"
          >
            Add
          </button>
        </div>

        <div className="flex flex-wrap gap-2 pt-2">
          {profile?.preferredLocations?.map((loc) => (
            <span
              key={loc}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-slate-800 text-slate-200 border border-slate-700 text-xs"
            >
              {loc}
              <button
                type="button"
                onClick={() => removeTag('preferredLocations', loc)}
                className="hover:text-rose-400"
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
