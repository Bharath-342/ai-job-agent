import React, { useState } from 'react';
import { profileApi } from '../services/api';
import { useNavigate } from 'react-router-dom';
import {
  UploadCloud,
  FileText,
  CheckCircle,
  AlertCircle,
  ArrowRight,
  ShieldCheck,
  User,
  Mail,
  Phone,
  GraduationCap,
  Code
} from 'lucide-react';

export const ResumeUploadPage = () => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [parsedProfile, setParsedProfile] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      setError(null);
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Please select a PDF or DOCX resume to upload.');
      return;
    }

    setUploading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await profileApi.uploadResume(formData);
      setParsedProfile(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to parse resume. Check file format.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto animate-fadeIn">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight">Resume Ingestion & Parsing</h1>
        <p className="text-xs text-slate-400">
          Upload your PDF or DOCX resume. The system extracts your profile, skills, and 2026 batch eligibility.
        </p>
      </div>

      {/* Upload Box */}
      <div className="glass-panel p-8 rounded-3xl border border-slate-800 text-center space-y-5">
        <div className="w-16 h-16 mx-auto rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
          <UploadCloud className="w-8 h-8" />
        </div>

        <div className="space-y-1">
          <h3 className="text-base font-bold text-white">Select or drag your Resume</h3>
          <p className="text-xs text-slate-400">Supported formats: PDF, DOCX (Max 10MB)</p>
        </div>

        <form onSubmit={handleUpload} className="max-w-md mx-auto space-y-4">
          <input
            type="file"
            accept=".pdf,.docx,.doc"
            onChange={handleFileChange}
            id="resume-file-input"
            className="block w-full text-xs text-slate-400 file:mr-4 file:py-2.5 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-indigo-600 file:text-white hover:file:bg-indigo-500 cursor-pointer"
          />

          {file && (
            <div className="flex items-center justify-center gap-2 text-xs font-semibold text-indigo-300 bg-indigo-950/40 p-2 rounded-xl border border-indigo-900/60">
              <FileText className="w-4 h-4" />
              {file.name} ({(file.size / 1024).toFixed(1)} KB)
            </div>
          )}

          {error && (
            <div className="flex items-center gap-2 p-3 rounded-xl bg-rose-950/50 border border-rose-800 text-xs text-rose-300 text-left">
              <AlertCircle className="w-4 h-4 shrink-0" />
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={!file || uploading}
            className="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-500 text-white font-semibold text-sm transition shadow-lg shadow-indigo-600/25 flex items-center justify-center gap-2"
          >
            {uploading ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                Analyzing Resume with PDFBox / POI...
              </>
            ) : (
              'Parse & Update Profile'
            )}
          </button>
        </form>
      </div>

      {/* Parsed Result Preview */}
      {parsedProfile && (
        <div className="glass-panel p-6 rounded-3xl border border-emerald-500/30 bg-emerald-950/10 space-y-5 animate-fadeIn">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <div className="flex items-center gap-2 text-emerald-400 font-bold text-sm">
              <CheckCircle className="w-5 h-5" />
              Resume Parsed Successfully!
            </div>
            <button
              onClick={() => navigate('/profile')}
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold shadow-sm transition"
            >
              Edit Profile
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <span className="text-slate-400 flex items-center gap-1">
                <User className="w-3.5 h-3.5" /> Name
              </span>
              <p className="font-bold text-white text-sm">{parsedProfile.fullName}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <span className="text-slate-400 flex items-center gap-1">
                <Mail className="w-3.5 h-3.5" /> Email
              </span>
              <p className="font-bold text-white text-sm">{parsedProfile.email}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <span className="text-slate-400 flex items-center gap-1">
                <GraduationCap className="w-3.5 h-3.5" /> Degree & Graduation
              </span>
              <p className="font-bold text-white text-sm">
                {parsedProfile.degree} ({parsedProfile.graduationYear} Batch)
              </p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <span className="text-slate-400 flex items-center gap-1">
                <Phone className="w-3.5 h-3.5" /> Phone
              </span>
              <p className="font-bold text-white text-sm">{parsedProfile.phone || 'Not found'}</p>
            </div>
          </div>

          {/* Extracted Skills */}
          <div className="space-y-2">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
              Extracted Skills ({parsedProfile.skills?.length || 0}):
            </span>
            <div className="flex flex-wrap gap-1.5">
              {parsedProfile.skills?.map((s) => (
                <span
                  key={s}
                  className="px-2.5 py-1 rounded-lg bg-indigo-950/60 text-indigo-300 border border-indigo-800 text-xs font-medium"
                >
                  {s}
                </span>
              ))}
            </div>
          </div>

          {/* Extracted Target Roles */}
          <div className="space-y-2">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
              Inferred Target Roles:
            </span>
            <div className="flex flex-wrap gap-1.5">
              {parsedProfile.targetRoles?.map((r) => (
                <span
                  key={r}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 text-slate-200 border border-slate-700 text-xs"
                >
                  {r}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
