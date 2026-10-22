import React, { useState, useEffect, useRef } from 'react';
import JSZip from 'jszip';
import {
  Download,
  Copy,
  Check,
  FileCode,
  FolderTree,
  Smartphone,
  ShieldCheck,
  BarChart3,
  Settings as SettingsIcon,
  Plus,
  RotateCcw,
  Sparkles,
  Info,
  Clock,
  Terminal,
  Search,
  CheckCircle2,
  AlertTriangle,
  HelpCircle,
  Heart,
  TrendingUp,
  Calculator,
  Gift,
  Timer,
  Shield,
  FileText,
  Calendar,
  Lock,
  Unlock,
  Bell,
  Sun,
  Moon,
  Upload,
  ArrowRight
} from 'lucide-react';
import { ANDROID_PROJECT_FILES, ProjectFile } from './androidProjectFiles.ts';

interface CigLog {
  id: number;
  timestamp: number;
  isSkipped: boolean; // Ghost Counter: true if user resisted a craving!
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'preview' | 'damage_report' | 'wishlist' | 'files' | 'workflow' | 'guide'>('preview');

  // Simulated Room DB State with Ghost Counter
  const [logs, setLogs] = useState<CigLog[]>(() => {
    const now = Date.now();
    const dayMillis = 24 * 60 * 60 * 1000;
    return [
      { id: 1, timestamp: now - 5 * dayMillis - 3600000 * 3, isSkipped: false },
      { id: 2, timestamp: now - 5 * dayMillis - 3600000 * 6, isSkipped: false },
      { id: 3, timestamp: now - 4 * dayMillis - 3600000 * 4, isSkipped: false },
      { id: 4, timestamp: now - 4 * dayMillis - 3600000 * 8, isSkipped: true }, // resisted
      { id: 5, timestamp: now - 3 * dayMillis - 3600000 * 2, isSkipped: false },
      { id: 6, timestamp: now - 2 * dayMillis - 3600000 * 5, isSkipped: false },
      { id: 7, timestamp: now - 2 * dayMillis - 3600000 * 7, isSkipped: true }, // resisted
      { id: 8, timestamp: now - 1 * dayMillis - 3600000 * 3, isSkipped: false },
      { id: 9, timestamp: now - 3600000 * 4, isSkipped: false },
      { id: 10, timestamp: now - 3600000 * 2, isSkipped: true }  // resisted
    ];
  });

  // Settings & New Features State
  const [pricePerUnit, setPricePerUnit] = useState<number>(10.0);
  const [priceInputText, setPriceInputText] = useState<string>("10.0");
  const [priceError, setPriceError] = useState<string | null>(null);

  // Feature 6: Wishlist Goal State
  const [wishlistTitle, setWishlistTitle] = useState<string>("Wireless Headphones");
  const [wishlistCost, setWishlistCost] = useState<number>(5000);

  // Feature 7: Nothing OS Widget Theme
  const [widgetTheme, setWidgetTheme] = useState<'dark' | 'white'>('dark');

  // Feature 9: PIN Lock State
  const [pinCode, setPinCode] = useState<string>("");
  const [isPhoneUnlocked, setIsPhoneUnlocked] = useState<boolean>(true);
  const [pinEntered, setPinEntered] = useState<string>("");
  const [pinError, setPinError] = useState<boolean>(false);

  // Navigation & Modals
  const [phoneScreen, setPhoneScreen] = useState<'home' | 'history' | 'settings'>('home');
  const [historyDays, setHistoryDays] = useState<number>(7);
  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
  const [showDonateModal, setShowDonateModal] = useState<boolean>(false);
  const [showEditPastDaysModal, setShowEditPastDaysModal] = useState<boolean>(false);
  const [showDamageReportModal, setShowDamageReportModal] = useState<boolean>(false);
  const [showBackupModal, setShowBackupModal] = useState<boolean>(false);
  const [backupJsonString, setBackupJsonString] = useState<string>("");
  const [snackbar, setSnackbar] = useState<string | null>(null);
  const [isPressingPlus, setIsPressingPlus] = useState<boolean>(false);
  const [copiedUpi, setCopiedUpi] = useState<boolean>(false);

  // Feature 4: Live Timer Clock (ticks every second)
  const [nowMillis, setNowMillis] = useState<number>(Date.now());

  useEffect(() => {
    const timer = setInterval(() => setNowMillis(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);

  // File explorer state
  const [selectedFile, setSelectedFile] = useState<ProjectFile>(ANDROID_PROJECT_FILES[0]);
  const [fileSearch, setFileSearch] = useState<string>('');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [copiedFile, setCopiedFile] = useState<boolean>(false);
  const [isZipping, setIsZipping] = useState<boolean>(false);
  const [zipSuccess, setZipSuccess] = useState<boolean>(false);

  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (snackbar) {
      const timer = setTimeout(() => setSnackbar(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [snackbar]);

  // Date math
  const startOfToday = new Date();
  startOfToday.setHours(0, 0, 0, 0);
  const startOfTodayMillis = startOfToday.getTime();

  const startOfWeek = new Date();
  startOfWeek.setDate(startOfWeek.getDate() - 6);
  startOfWeek.setHours(0, 0, 0, 0);
  const startOfWeekMillis = startOfWeek.getTime();

  const startOfMonth = new Date();
  startOfMonth.setDate(startOfMonth.getDate() - 29);
  startOfMonth.setHours(0, 0, 0, 0);
  const startOfMonthMillis = startOfMonth.getTime();

  // Smoked logs vs Ghost Counter (Skipped logs)
  const smokedLogs = logs.filter(l => !l.isSkipped);
  const skippedLogs = logs.filter(l => l.isSkipped);

  const todaySmoked = smokedLogs.filter(l => l.timestamp >= startOfTodayMillis).length;
  const todaySkipped = skippedLogs.filter(l => l.timestamp >= startOfTodayMillis).length;

  const weekSmoked = smokedLogs.filter(l => l.timestamp >= startOfWeekMillis).length;
  const monthSmoked = smokedLogs.filter(l => l.timestamp >= startOfMonthMillis).length;

  const todaySpent = todaySmoked * pricePerUnit;
  const todaySavedBySkipping = todaySkipped * pricePerUnit;
  const weekSpent = weekSmoked * pricePerUnit;
  const monthSpent = monthSmoked * pricePerUnit;

  // Feature 4: Live Timer (Duration since latest smoked cigarette)
  const latestSmoked = smokedLogs.length > 0
    ? smokedLogs.reduce((latest, curr) => (curr.timestamp > latest.timestamp ? curr : latest))
    : null;
  const elapsedSecondsSinceSmoked = latestSmoked ? Math.max(0, Math.floor((nowMillis - latestSmoked.timestamp) / 1000)) : null;

  const formatTimer = (totalSec: number | null) => {
    if (totalSec === null) return "No smoke logged yet";
    const h = Math.floor(totalSec / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = totalSec % 60;
    return `${String(h).padStart(2, '0')}h ${String(m).padStart(2, '0')}m ${String(s).padStart(2, '0')}s`;
  };

  // Feature 3: "The Mirror" — Pace Projection
  const weekDailyAvg = weekSmoked / 7.0;
  const mirrorMonthlyCigs = Math.round(weekDailyAvg * 30);
  const mirrorYearlyCigs = Math.round(weekDailyAvg * 365);
  const mirrorYearlyUnits = mirrorYearlyCigs * pricePerUnit;

  // Feature 6: Wishlist Calculations
  const dailyReductionBenefit = 2.0 * pricePerUnit;
  const wishlistDays = dailyReductionBenefit > 0 ? Math.ceil(wishlistCost / dailyReductionBenefit) : 34;

  // Logging functions
  const logCigarette = () => {
    const newLog: CigLog = { id: Date.now(), timestamp: Date.now(), isSkipped: false };
    setLogs(prev => [...prev, newLog]);
    setSnackbar("Logged 1 smoked cigarette (+1)");
  };

  // Feature 1: Ghost Counter Log
  const logSkippedOne = () => {
    const newLog: CigLog = { id: Date.now(), timestamp: Date.now(), isSkipped: true };
    setLogs(prev => [...prev, newLog]);
    setSnackbar("Craving defeated! Ghost Counter updated (+1). You saved " + formatUnits(pricePerUnit) + " units!");
  };

  // Feature 5: Edit Past Days
  const addPastDayLogs = (daysAgo: number, count: number, isSkipped: boolean = false) => {
    const dayMillis = 24 * 60 * 60 * 1000;
    const targetTimestamp = Date.now() - daysAgo * dayMillis;
    const newItems: CigLog[] = [];
    for (let i = 0; i < count; i++) {
      newItems.push({ id: Date.now() + i, timestamp: targetTimestamp, isSkipped });
    }
    setLogs(prev => [...prev, ...newItems].sort((a, b) => a.timestamp - b.timestamp));
    setShowEditPastDaysModal(false);
    setSnackbar(`Added ${count} log(s) to ${daysAgo === 1 ? 'yesterday' : `${daysAgo} days ago`}.`);
  };

  const undoLast = () => {
    if (logs.length === 0) {
      setSnackbar("No logs found to undo.");
      return;
    }
    setLogs(prev => prev.slice(0, prev.length - 1));
    setSnackbar("Removed last logged entry.");
  };

  const deleteAll = () => {
    setLogs([]);
    setShowDeleteModal(false);
    setSnackbar("All cigarette history deleted.");
  };

  const handleSavePrice = () => {
    const trimmed = priceInputText.trim();
    if (!trimmed) {
      setPriceError("Price cannot be empty.");
      return;
    }
    const val = parseFloat(trimmed);
    if (isNaN(val) || val <= 0) {
      setPriceError("Please enter a valid positive number.");
      return;
    }
    setPricePerUnit(val);
    setPriceError(null);
    setSnackbar(`Price saved: ${formatUnits(val)} units per cig`);
  };

  // Feature 8: Simulate 9 PM Local Evening Notification
  const triggerEveningSummaryNotification = () => {
    const yesterdaySmoked = smokedLogs.filter(l => {
      const yesterdayStart = startOfTodayMillis - 24 * 3600 * 1000;
      return l.timestamp >= yesterdayStart && l.timestamp < startOfTodayMillis;
    }).length;

    let msg = `9:00 PM Summary: Today: ${todaySmoked} cigs, ${formatUnits(todaySpent)} units.`;
    if (todaySmoked < yesterdaySmoked) {
      msg += ` (${yesterdaySmoked - todaySmoked} less than yesterday! 🎉)`;
    } else if (todaySmoked === 0) {
      msg += ` (Smoke-free day! Milestone achieved! 🌟)`;
    } else {
      msg += ` Rest well, tomorrow is a fresh start.`;
    }
    setSnackbar(msg);
  };

  // Feature 9: Export JSON Backup
  const handleExportBackup = () => {
    const backupObj = {
      app: "SmokeLedger",
      version: "1.2.0",
      exportedAt: new Date().toISOString(),
      pricePerCigarette: pricePerUnit,
      wishlistTitle,
      wishlistCost,
      logs
    };
    const jsonStr = JSON.stringify(backupObj, null, 2);
    setBackupJsonString(jsonStr);
    setShowBackupModal(true);
  };

  // Canvas drawing for the Compose Bar Chart simulation
  useEffect(() => {
    if (phoneScreen !== 'history') return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    const width = rect.width;
    const height = rect.height;
    ctx.clearRect(0, 0, width, height);

    const numDays = historyDays;
    const dayData: { label: string; count: number; skipped: number }[] = [];

    for (let i = numDays - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      d.setHours(0, 0, 0, 0);
      const start = d.getTime();
      const end = start + 24 * 60 * 60 * 1000 - 1;
      const count = smokedLogs.filter(l => l.timestamp >= start && l.timestamp <= end).length;
      const skipped = skippedLogs.filter(l => l.timestamp >= start && l.timestamp <= end).length;
      const label = numDays <= 7
        ? d.toLocaleDateString('en-US', { weekday: 'short' })
        : `${d.getMonth() + 1}/${d.getDate()}`;
      dayData.push({ label, count, skipped });
    }

    const maxCount = Math.max(...dayData.map(d => d.count), 5);
    const bottomMargin = 28;
    const topMargin = 24;
    const chartHeight = height - bottomMargin - topMargin;
    const slotWidth = width / numDays;
    const barWidth = Math.min(slotWidth * 0.6, 22);

    ctx.strokeStyle = '#3f3f46';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(0, height - bottomMargin);
    ctx.lineTo(width, height - bottomMargin);
    ctx.stroke();

    dayData.forEach((d, idx) => {
      const centerX = idx * slotWidth + slotWidth / 2;
      const barH = (d.count / maxCount) * chartHeight;
      const x = centerX - barWidth / 2;
      const y = height - bottomMargin - (d.count > 0 ? barH : 3);

      ctx.fillStyle = d.count > 0 ? '#ff6e40' : '#27272a';
      ctx.beginPath();
      ctx.roundRect(x, y, barWidth, d.count > 0 ? barH : 3, [4, 4, 0, 0]);
      ctx.fill();

      if (d.count > 0 && (numDays <= 7 || idx % 4 === 0)) {
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 10px monospace';
        ctx.textAlign = 'center';
        ctx.fillText(String(d.count), centerX, y - 5);
      }

      if (numDays <= 7 || idx % 5 === 0 || idx === numDays - 1) {
        ctx.fillStyle = '#a1a1aa';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(d.label, centerX, height - 10);
      }
    });
  }, [phoneScreen, logs, historyDays]);

  function formatUnits(val: number): string {
    return val % 1 === 0 ? val.toLocaleString() : val.toLocaleString(undefined, { minimumFractionDigits: 1, maximumFractionDigits: 2 });
  }

  const copyUpiId = async () => {
    try {
      await navigator.clipboard.writeText("puneetfsn@ybl");
      setCopiedUpi(true);
      setSnackbar("Copied UPI ID: puneetfsn@ybl");
      setTimeout(() => setCopiedUpi(false), 2500);
    } catch {
      setSnackbar("UPI ID: puneetfsn@ybl");
    }
  };

  const handleCopyCode = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedFile(true);
      setTimeout(() => setCopiedFile(false), 2000);
    } catch {
      // fallback
    }
  };

  const handleDownloadZip = async () => {
    setIsZipping(true);
    try {
      const zip = new JSZip();
      ANDROID_PROJECT_FILES.forEach(file => {
        zip.file(file.path, file.content);
      });
      const blob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'SmokeLedger-Complete-Android-Project.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      setZipSuccess(true);
      setTimeout(() => setZipSuccess(false), 4000);
    } catch (err) {
      console.error("Failed to generate zip", err);
    } finally {
      setIsZipping(false);
    }
  };

  const filteredFiles = ANDROID_PROJECT_FILES.filter(f => {
    const matchesSearch = f.path.toLowerCase().includes(fileSearch.toLowerCase()) ||
                          f.description.toLowerCase().includes(fileSearch.toLowerCase());
    const matchesCategory = categoryFilter === 'all' || f.category === categoryFilter;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="min-h-screen bg-[#0a0a0c] text-[#ededed] font-sans flex flex-col">
      {/* Top App Header */}
      <header className="border-b border-[#202026] bg-[#121216]/95 backdrop-blur-md sticky top-0 z-40 px-6 py-3.5 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#ff6e40] to-[#ff3b30] flex items-center justify-center shadow-lg shadow-[#ff6e40]/30 font-bold text-white text-xl">
            S
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-bold text-lg text-white tracking-tight">SmokeLedger</h1>
              <span className="text-[11px] font-semibold bg-[#27272a] text-[#ff6e40] px-2 py-0.5 rounded-full border border-[#3f3f46]">
                Nothing OS Native
              </span>
              <span className="text-[11px] font-medium bg-[#1e293b] text-[#38bdf8] px-2 py-0.5 rounded-full border border-[#38bdf8]/30">
                100% Offline
              </span>
            </div>
            <p className="text-xs text-[#a1a1aa]">Ghost Counter • Damage Report PDF • The Mirror • Live Timer • Wishlist</p>
          </div>
        </div>

        {/* Global Navigation Tabs */}
        <div className="flex items-center bg-[#1c1c21] p-1 rounded-xl border border-[#2a2a32]">
          <button
            onClick={() => setActiveTab('preview')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'preview' ? 'bg-[#ff6e40] text-white shadow-sm' : 'text-[#a1a1aa] hover:text-white'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            Live App &amp; Widget
          </button>
          <button
            onClick={() => setActiveTab('damage_report')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'damage_report' ? 'bg-[#ff6e40] text-white shadow-sm' : 'text-[#a1a1aa] hover:text-white'
            }`}
          >
            <FileText className="w-3.5 h-3.5" />
            Damage Report PDF
          </button>
          <button
            onClick={() => setActiveTab('wishlist')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'wishlist' ? 'bg-[#ff6e40] text-white shadow-sm' : 'text-[#a1a1aa] hover:text-white'
            }`}
          >
            <Gift className="w-3.5 h-3.5" />
            Wishlist &amp; The Mirror
          </button>
          <button
            onClick={() => setActiveTab('files')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'files' ? 'bg-[#ff6e40] text-white shadow-sm' : 'text-[#a1a1aa] hover:text-white'
            }`}
          >
            <FolderTree className="w-3.5 h-3.5" />
            Files ({ANDROID_PROJECT_FILES.length})
          </button>
          <button
            onClick={() => setActiveTab('guide')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'guide' ? 'bg-[#ff6e40] text-white shadow-sm' : 'text-[#a1a1aa] hover:text-white'
            }`}
          >
            <HelpCircle className="w-3.5 h-3.5" />
            Install Guide
          </button>
        </div>

        {/* Action Buttons: Donate & Download ZIP */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowDonateModal(true)}
            className="flex items-center gap-1.5 bg-[#f59e0b]/15 hover:bg-[#f59e0b]/25 border border-[#f59e0b]/40 text-[#f59e0b] px-3 py-2 rounded-xl text-xs font-semibold transition"
          >
            <Heart className="w-3.5 h-3.5 fill-[#f59e0b]" />
            Donate (puneetfsn@ybl)
          </button>

          <button
            onClick={handleDownloadZip}
            disabled={isZipping}
            className="flex items-center gap-2 bg-[#ff6e40] hover:bg-[#ff8a65] text-white px-4 py-2 rounded-xl text-xs font-semibold transition shadow-md shadow-[#ff6e40]/25 active:scale-95 disabled:opacity-50"
          >
            <Download className="w-3.5 h-3.5" />
            {isZipping ? "Creating ZIP..." : zipSuccess ? "Downloaded!" : "Download Android ZIP"}
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 p-6 max-w-7xl mx-auto w-full">
        {/* TAB 1: LIVE SIMULATOR */}
        {activeTab === 'preview' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            {/* Left 7 cols: Mobile Phone Frame */}
            <div className="lg:col-span-7 flex flex-col items-center">
              <div className="w-full max-w-[390px] bg-[#101012] border-[8px] border-[#222226] rounded-[44px] shadow-2xl overflow-hidden flex flex-col relative h-[830px]">
                {/* Android Status Bar */}
                <div className="px-6 pt-3 pb-2 flex justify-between items-center text-xs text-[#a1a1aa] select-none bg-[#101012]">
                  <span className="font-semibold text-white">10:42</span>
                  <div className="flex items-center gap-2">
                    {pinCode && <Lock className="w-3 h-3 text-[#ff6e40]" />}
                    <span className="text-[10px]">5G</span>
                    <div className="w-5 h-2.5 border border-[#a1a1aa] rounded-sm p-0.5 flex items-center">
                      <div className="h-full bg-white w-full rounded-2xs"></div>
                    </div>
                  </div>
                </div>

                {/* FEATURE 9: PIN LOCK SCREEN SIMULATION */}
                {pinCode && !isPhoneUnlocked ? (
                  <div className="flex-1 flex flex-col items-center justify-center p-6 text-center bg-[#0d0d10] space-y-4">
                    <div className="w-16 h-16 rounded-full bg-[#1e1e24] flex items-center justify-center border border-[#2a2a32]">
                      <Lock className="w-8 h-8 text-[#ff6e40]" />
                    </div>
                    <h3 className="font-bold text-white text-base">SmokeLedger Locked</h3>
                    <p className="text-xs text-[#a1a1aa] max-w-[220px]">
                      Your habit logs are private. Enter your 4-digit PIN to access:
                    </p>

                    <div className="w-48">
                      <input
                        type="password"
                        maxLength={4}
                        value={pinEntered}
                        onChange={(e) => {
                          setPinEntered(e.target.value);
                          setPinError(false);
                          if (e.target.value === pinCode) {
                            setIsPhoneUnlocked(true);
                            setPinEntered("");
                            setSnackbar("Unlocked!");
                          } else if (e.target.value.length === 4) {
                            setPinError(true);
                          }
                        }}
                        placeholder="••••"
                        className="w-full text-center tracking-[12px] text-2xl font-mono bg-[#1c1c21] border border-[#2a2a32] rounded-xl py-2.5 text-white focus:outline-none focus:border-[#ff6e40]"
                      />
                      {pinError && (
                        <p className="text-xs text-[#f87171] mt-1.5 font-medium">Incorrect PIN</p>
                      )}
                    </div>
                  </div>
                ) : (
                  /* SIMULATED SCREEN CONTENT */
                  <div className="flex-1 overflow-y-auto px-5 py-3 relative bg-[#0c0c0e]">
                    {/* Toast Notification */}
                    {snackbar && (
                      <div className="absolute top-2 left-4 right-4 bg-[#202026] text-white text-xs px-3.5 py-2.5 rounded-xl border border-[#34343e] shadow-lg flex items-center gap-2 z-30 animate-in fade-in slide-in-from-top-2">
                        <CheckCircle2 className="w-4 h-4 text-[#ff6e40] shrink-0" />
                        <span className="flex-1 font-medium">{snackbar}</span>
                      </div>
                    )}

                    {/* SCREEN 1: HOME */}
                    {phoneScreen === 'home' && (
                      <div className="flex flex-col items-center text-center">
                        <h2 className="text-xl font-bold text-white mt-1">SmokeLedger</h2>
                        <p className="text-xs text-[#a1a1aa] mt-0.5 mb-4">Zero guilt • Pure math • Habit shift</p>

                        {/* FEATURE 4: LIVE TIMER CARD */}
                        <div className="w-full bg-[#18181c] border border-[#26262e] rounded-2xl p-3.5 mb-4 text-left flex items-center gap-3">
                          <div className="w-9 h-9 rounded-xl bg-[#22c55e]/15 flex items-center justify-center border border-[#22c55e]/30 shrink-0">
                            <Timer className="w-5 h-5 text-[#22c55e]" />
                          </div>
                          <div>
                            <div className="text-[10px] text-[#71717a] font-medium uppercase tracking-wider">
                              SMOKE-FREE DURATION
                            </div>
                            <div className="text-sm font-mono font-bold text-[#22c55e]">
                              {formatTimer(elapsedSecondsSinceSmoked)}
                            </div>
                          </div>
                        </div>

                        {/* HERO COUNTER: SMOKED VS GHOST COUNTER */}
                        <div className="w-full bg-[#141418] border border-[#222228] rounded-2xl p-4 mb-5">
                          <div className="grid grid-cols-2 divide-x divide-[#222228]">
                            {/* Left: Smoked */}
                            <div className="pr-3">
                              <div className="text-[10px] font-bold text-[#71717a] tracking-wider uppercase mb-0.5">
                                TODAY SMOKED
                              </div>
                              <div className={`text-4xl font-extrabold ${todaySmoked > 0 ? 'text-[#ff6e40]' : 'text-white'}`}>
                                {todaySmoked}
                              </div>
                              <div className="text-[11px] text-[#a1a1aa] mt-0.5">
                                {formatUnits(todaySpent)} units spent
                              </div>
                            </div>

                            {/* Right: FEATURE 1: Ghost Counter */}
                            <div className="pl-3">
                              <div className="text-[10px] font-bold text-[#38bdf8] tracking-wider uppercase mb-0.5 flex items-center justify-center gap-1">
                                <Shield className="w-3 h-3 text-[#38bdf8]" />
                                GHOST COUNTER
                              </div>
                              <div className="text-4xl font-extrabold text-[#38bdf8]">
                                {todaySkipped}
                              </div>
                              <div className="text-[11px] text-[#38bdf8] mt-0.5">
                                {formatUnits(todaySavedBySkipping)} units saved!
                              </div>
                            </div>
                          </div>
                        </div>

                        {/* DUAL ACTION BUTTONS: Smoked (+) and Resisted Cravings (Ghost) */}
                        <div className="flex items-center justify-center gap-6 mb-4">
                          {/* Smoked Button */}
                          <div className="flex flex-col items-center">
                            <button
                              onMouseDown={() => setIsPressingPlus(true)}
                              onMouseUp={() => {
                                setIsPressingPlus(false);
                                logCigarette();
                              }}
                              className={`w-20 h-20 rounded-full bg-[#ff6e40] hover:bg-[#ff8a65] text-white flex items-center justify-center shadow-lg shadow-[#ff6e40]/30 transition-transform ${
                                isPressingPlus ? 'scale-90' : 'scale-100 hover:scale-105'
                              }`}
                            >
                              <Plus className="w-10 h-10 stroke-[2.5]" />
                            </button>
                            <span className="text-[11px] text-[#a1a1aa] mt-2 font-medium">I Smoked</span>
                          </div>

                          {/* FEATURE 1: Ghost Counter Resisted Button */}
                          <div className="flex flex-col items-center">
                            <button
                              onClick={logSkippedOne}
                              className="w-20 h-20 rounded-full bg-[#0369a1] hover:bg-[#0284c7] text-white flex items-center justify-center shadow-lg shadow-[#0284c7]/30 transition-transform active:scale-90"
                              title="Ghost Counter: Log that you resisted a craving!"
                            >
                              <Shield className="w-8 h-8" />
                            </button>
                            <span className="text-[11px] text-[#38bdf8] mt-2 font-medium">Skipped One!</span>
                          </div>
                        </div>

                        {/* Quick Controls: Undo & FEATURE 5: Edit Past Days */}
                        <div className="flex items-center gap-2 mb-5">
                          <button
                            onClick={undoLast}
                            className="flex items-center gap-1 px-3 py-1.5 rounded-full border border-[#2a2a32] text-[11px] text-[#a1a1aa] hover:text-white hover:border-[#3f3f46] transition"
                          >
                            <RotateCcw className="w-3 h-3" />
                            Undo
                          </button>
                          <button
                            onClick={() => setShowEditPastDaysModal(true)}
                            className="flex items-center gap-1 px-3 py-1.5 rounded-full border border-[#2a2a32] text-[11px] text-[#a1a1aa] hover:text-white hover:border-[#3f3f46] transition"
                          >
                            <Calendar className="w-3 h-3 text-[#ff6e40]" />
                            Edit Past Day
                          </button>
                        </div>

                        {/* FEATURE 3: "THE MIRROR" PACE PROJECTION */}
                        <div className="w-full bg-[#141418] border border-[#222228] rounded-2xl p-4 text-left mb-4">
                          <div className="flex items-center gap-2 text-[#f59e0b] mb-1">
                            <TrendingUp className="w-4 h-4" />
                            <span className="font-semibold text-xs text-white">The Mirror — Pace Projection</span>
                          </div>
                          <p className="text-[10px] text-[#a1a1aa] mb-2.5">
                            Based on your 7-day average pace ({weekDailyAvg.toFixed(1)}/day):
                          </p>

                          <div className="bg-[#1b1b22] p-2.5 rounded-xl border border-[#2a2a34] flex items-center justify-between text-xs">
                            <div>
                              <div className="text-[10px] text-[#71717a]">Monthly pace</div>
                              <div className="font-bold text-white mt-0.5">≈ {mirrorMonthlyCigs} cigs</div>
                            </div>
                            <ArrowRight className="w-3.5 h-3.5 text-[#71717a]" />
                            <div className="text-right">
                              <div className="text-[10px] text-[#71717a]">Annual pace</div>
                              <div className="font-bold text-[#ff6e40] mt-0.5">
                                {formatUnits(mirrorYearlyUnits)} units
                              </div>
                            </div>
                          </div>
                        </div>

                        {/* FEATURE 6: WISHLIST MOTIVATION */}
                        <div className="w-full bg-[#141418] border border-[#222228] rounded-2xl p-4 text-left mb-4">
                          <div className="flex items-center justify-between mb-1">
                            <div className="flex items-center gap-2 text-[#a78bfa]">
                              <Gift className="w-4 h-4" />
                              <span className="font-semibold text-xs text-white">Wishlist Dream Goal</span>
                            </div>
                            <span className="text-[10px] text-[#a78bfa] font-mono font-semibold">
                              {formatUnits(wishlistCost)} units
                            </span>
                          </div>
                          <div className="text-xs font-semibold text-white mb-2">{wishlistTitle}</div>
                          <div className="bg-[#1c192e] border border-[#a78bfa]/30 rounded-xl p-2.5 text-[11px] text-[#c4b5fd]">
                            🎧 <strong>Smoke 2 less per day</strong> = {wishlistTitle} in <strong>{wishlistDays} days</strong>!
                          </div>
                        </div>

                        {/* Positive Shift Tasks */}
                        <div className="w-full bg-[#141418] border border-[#222228] rounded-2xl p-3.5 text-left mb-4">
                          <div className="flex justify-between items-center mb-1">
                            <span className="font-semibold text-xs text-white flex items-center gap-1.5">
                              <Sparkles className="w-3.5 h-3.5 text-[#ff6e40]" />
                              Positive Shift Tasks
                            </span>
                            <span className="text-[9px] text-[#f59e0b] bg-[#222228] px-2 py-0.5 rounded-md">
                              {todaySmoked >= 8 ? "Micro-Habits" : "Gentle"}
                            </span>
                          </div>
                          <div className="text-[10px] text-[#a1a1aa] mb-2">
                            {todaySmoked >= 8
                              ? "Drink 1 glass of cool water • 5 slow deep breaths • Drop shoulders"
                              : "Call mom/friend • 10m stretch • Fresh veg juice • Gift wife"}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* SCREEN 2: HISTORY */}
                    {phoneScreen === 'history' && (
                      <div className="flex flex-col text-left space-y-4">
                        <div>
                          <h2 className="text-xl font-bold text-white">History &amp; Trends</h2>
                          <p className="text-xs text-[#a1a1aa] mt-0.5">Track your daily patterns over time.</p>
                        </div>

                        <div className="flex bg-[#1c1c21] p-1 rounded-xl border border-[#2a2a32]">
                          <button
                            onClick={() => setHistoryDays(7)}
                            className={`flex-1 py-1.5 text-xs font-medium rounded-lg transition ${
                              historyDays === 7 ? 'bg-[#ff6e40] text-white shadow' : 'text-[#a1a1aa]'
                            }`}
                          >
                            Last 7 Days
                          </button>
                          <button
                            onClick={() => setHistoryDays(30)}
                            className={`flex-1 py-1.5 text-xs font-medium rounded-lg transition ${
                              historyDays === 30 ? 'bg-[#ff6e40] text-white shadow' : 'text-[#a1a1aa]'
                            }`}
                          >
                            Last 30 Days
                          </button>
                        </div>

                        {/* Compose Canvas Bar Chart */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4">
                          <div className="flex justify-between items-center mb-3">
                            <span className="font-semibold text-sm text-white">Daily Count</span>
                            <span className="text-xs text-[#ff6e40] font-bold">
                              Avg: {(smokedLogs.length / historyDays).toFixed(1)}/day
                            </span>
                          </div>
                          <canvas ref={canvasRef} className="w-full h-44 rounded-lg block" />
                        </div>

                        {/* Recent entries list */}
                        <div>
                          <div className="text-xs font-semibold text-white mb-2">Recent Logs ({logs.length})</div>
                          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
                            {[...logs].reverse().slice(0, 15).map(log => {
                              const d = new Date(log.timestamp);
                              return (
                                <div key={log.id} className="bg-[#141418] border border-[#222228] rounded-xl px-3 py-2 flex items-center justify-between text-xs">
                                  <div className="flex items-center gap-2">
                                    <div className={`w-2 h-2 rounded-full ${log.isSkipped ? 'bg-[#38bdf8]' : 'bg-[#ff6e40]'}`}></div>
                                    <span className="text-[#ededed]">{log.isSkipped ? 'Resisted (Ghost)' : 'Smoked 1'}</span>
                                  </div>
                                  <span className="text-[11px] text-[#a1a1aa]">
                                    {d.toLocaleDateString([], { month: 'short', day: 'numeric' })} {d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                  </span>
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* SCREEN 3: SETTINGS */}
                    {phoneScreen === 'settings' && (
                      <div className="flex flex-col text-left space-y-4">
                        <h2 className="text-xl font-bold text-white">Settings</h2>

                        {/* FEATURE 2: DAMAGE REPORT ACTION */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4 space-y-2">
                          <div className="flex items-center gap-2 text-[#ff6e40]">
                            <FileText className="w-4 h-4" />
                            <span className="font-semibold text-xs text-white">Damage Report (PDF)</span>
                          </div>
                          <p className="text-[11px] text-[#a1a1aa] leading-relaxed">
                            Generate a clean clinical PDF for your doctor or therapist with 30-day totals, pace, and averages.
                          </p>
                          <button
                            onClick={() => setShowDamageReportModal(true)}
                            className="w-full py-2 bg-[#ff6e40] hover:bg-[#ff8a65] text-white rounded-xl text-xs font-semibold transition"
                          >
                            View &amp; Generate Damage Report
                          </button>
                        </div>

                        {/* FEATURE 7: NOTHING OS WIDGET THEME */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4 space-y-2">
                          <span className="font-semibold text-xs text-white">Nothing OS Widget Theme</span>
                          <div className="flex gap-2">
                            <button
                              onClick={() => {
                                setWidgetTheme('dark');
                                setSnackbar("Widget set to Matrix Dark (Obsidian)");
                              }}
                              className={`flex-1 py-1.5 text-xs rounded-xl border flex items-center justify-center gap-1.5 transition ${
                                widgetTheme === 'dark' ? 'bg-[#27272a] border-[#ff6e40] text-white' : 'border-[#2a2a32] text-[#a1a1aa]'
                              }`}
                            >
                              <Moon className="w-3.5 h-3.5" />
                              Matrix Dark
                            </button>
                            <button
                              onClick={() => {
                                setWidgetTheme('white');
                                setSnackbar("Widget set to Nothing Light (Monochrome)");
                              }}
                              className={`flex-1 py-1.5 text-xs rounded-xl border flex items-center justify-center gap-1.5 transition ${
                                widgetTheme === 'white' ? 'bg-white text-black border-white font-semibold' : 'border-[#2a2a32] text-[#a1a1aa]'
                              }`}
                            >
                              <Sun className="w-3.5 h-3.5" />
                              Nothing Light
                            </button>
                          </div>
                        </div>

                        {/* FEATURE 9: PIN LOCK & PRIVACY */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4 space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="font-semibold text-xs text-white flex items-center gap-1.5">
                              <Lock className="w-3.5 h-3.5 text-[#f59e0b]" />
                              PIN Privacy Lock
                            </span>
                            <span className="text-[10px] text-[#71717a]">{pinCode ? "Active (4 Digits)" : "Disabled"}</span>
                          </div>
                          <div className="flex gap-2">
                            <input
                              type="password"
                              maxLength={4}
                              placeholder={pinCode ? "Change PIN" : "Set 4-digit PIN"}
                              value={pinCode}
                              onChange={(e) => {
                                setPinCode(e.target.value);
                                setSnackbar(e.target.value ? "PIN enabled!" : "PIN removed");
                              }}
                              className="flex-1 bg-[#1b1b22] border border-[#2a2a34] rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-[#ff6e40]"
                            />
                            {pinCode && (
                              <button
                                onClick={() => {
                                  setIsPhoneUnlocked(false);
                                  setSnackbar("Screen locked! Enter PIN to unlock.");
                                }}
                                className="px-3 py-1.5 bg-[#27272a] text-white text-xs rounded-xl hover:bg-[#3f3f46]"
                              >
                                Lock Now
                              </button>
                            )}
                          </div>
                        </div>

                        {/* FEATURE 8: EVENING NOTIFICATION TESTER */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4 flex items-center justify-between">
                          <div>
                            <div className="text-xs font-semibold text-white flex items-center gap-1.5">
                              <Bell className="w-3.5 h-3.5 text-[#ff6e40]" />
                              9 PM Local Summary
                            </div>
                            <div className="text-[10px] text-[#a1a1aa]">On-device daily notification</div>
                          </div>
                          <button
                            onClick={triggerEveningSummaryNotification}
                            className="px-2.5 py-1.5 bg-[#27272a] hover:bg-[#3f3f46] text-white text-xs rounded-xl transition"
                          >
                            Test Notification
                          </button>
                        </div>

                        {/* Local Backup */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4 space-y-2">
                          <span className="font-semibold text-xs text-white flex items-center gap-1.5">
                            <Upload className="w-3.5 h-3.5 text-[#38bdf8]" />
                            Local JSON Backup &amp; Restore
                          </span>
                          <p className="text-[10px] text-[#a1a1aa]">Export all your data offline without cloud access.</p>
                          <button
                            onClick={handleExportBackup}
                            className="w-full py-2 bg-[#202028] hover:bg-[#282834] text-white rounded-xl text-xs font-medium border border-[#2e2e3a] transition"
                          >
                            Export JSON Backup File
                          </button>
                        </div>

                        {/* Cost Configuration */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4 space-y-2">
                          <span className="font-semibold text-xs text-white">Price per Cigarette (Units)</span>
                          <div className="flex gap-2">
                            <input
                              type="text"
                              value={priceInputText}
                              onChange={(e) => {
                                setPriceInputText(e.target.value);
                                setPriceError(null);
                              }}
                              className="flex-1 bg-[#1b1b22] border border-[#2a2a34] rounded-xl px-3 py-1.5 text-xs text-white"
                            />
                            <button
                              onClick={handleSavePrice}
                              className="bg-[#ff6e40] text-white px-3 py-1.5 rounded-xl text-xs font-semibold"
                            >
                              Save
                            </button>
                          </div>
                          {priceError && <p className="text-[10px] text-[#f87171]">{priceError}</p>}
                        </div>

                        {/* Danger Zone */}
                        <div className="bg-[#141418] border border-[#222228] rounded-2xl p-4">
                          <button
                            onClick={() => setShowDeleteModal(true)}
                            className="w-full py-2 border border-[#f87171]/40 text-[#f87171] hover:bg-[#f87171]/10 rounded-xl text-xs font-semibold transition"
                          >
                            Delete All Data
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Bottom Navigation Bar */}
                <div className="bg-[#121216] border-t border-[#202026] py-2 px-6 flex justify-around items-center select-none">
                  <button
                    onClick={() => setPhoneScreen('home')}
                    className={`flex flex-col items-center gap-1 transition ${
                      phoneScreen === 'home' ? 'text-[#ff6e40]' : 'text-[#71717a] hover:text-[#a1a1aa]'
                    }`}
                  >
                    <Plus className="w-5 h-5" />
                    <span className="text-[10px] font-medium">Counter</span>
                  </button>
                  <button
                    onClick={() => setPhoneScreen('history')}
                    className={`flex flex-col items-center gap-1 transition ${
                      phoneScreen === 'history' ? 'text-[#ff6e40]' : 'text-[#71717a] hover:text-[#a1a1aa]'
                    }`}
                  >
                    <BarChart3 className="w-5 h-5" />
                    <span className="text-[10px] font-medium">History</span>
                  </button>
                  <button
                    onClick={() => setPhoneScreen('settings')}
                    className={`flex flex-col items-center gap-1 transition ${
                      phoneScreen === 'settings' ? 'text-[#ff6e40]' : 'text-[#71717a] hover:text-[#a1a1aa]'
                    }`}
                  >
                    <SettingsIcon className="w-5 h-5" />
                    <span className="text-[10px] font-medium">Settings</span>
                  </button>
                </div>
              </div>
            </div>

            {/* Right 5 cols: FEATURE 7: NOTHING OS WIDGET PREVIEW */}
            <div className="lg:col-span-5 space-y-6 text-left">
              <div className="bg-[#121216] border border-[#202026] rounded-3xl p-6 shadow-xl">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h3 className="font-bold text-white text-base flex items-center gap-2">
                      <Sparkles className="w-4 h-4 text-[#ff6e40]" />
                      Nothing OS Widget (NDot Aesthetic)
                    </h3>
                    <p className="text-xs text-[#a1a1aa] mt-0.5">
                      Theme: {widgetTheme === 'white' ? "Nothing Light (White Monochrome)" : "Matrix Dark (Obsidian)"}
                    </p>
                  </div>
                  {/* Theme Switcher */}
                  <div className="flex bg-[#1c1c21] p-1 rounded-xl border border-[#2a2a32]">
                    <button
                      onClick={() => setWidgetTheme('dark')}
                      className={`p-1.5 rounded-lg transition ${widgetTheme === 'dark' ? 'bg-[#27272a] text-[#ff6e40]' : 'text-[#71717a]'}`}
                      title="Matrix Dark Theme"
                    >
                      <Moon className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => setWidgetTheme('white')}
                      className={`p-1.5 rounded-lg transition ${widgetTheme === 'white' ? 'bg-white text-black' : 'text-[#71717a]'}`}
                      title="Nothing Light Theme"
                    >
                      <Sun className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                {/* NOTHING OS WIDGET MOCKUP */}
                <div className={`p-4 rounded-3xl border-2 transition-all ${
                  widgetTheme === 'white'
                    ? 'bg-[#e4e4e7] border-white shadow-xl'
                    : 'bg-[#09090b] border-[#ff3b30]/40 shadow-2xl'
                }`}>
                  <div className={`rounded-2xl p-4 transition-colors ${
                    widgetTheme === 'white'
                      ? 'bg-[#f4f4f5] text-[#09090b] border border-[#d4d4d8]'
                      : 'bg-[#141416] text-[#ededed] border border-[#222228]'
                  }`}>
                    {/* Top Row: Dual Action Buttons & Stats */}
                    <div className="flex items-center gap-4">
                      {/* Left Action Buttons */}
                      <div className="flex flex-col items-center gap-1.5">
                        <button
                          onClick={logCigarette}
                          className="w-14 h-14 rounded-full bg-[#ff3b30] hover:bg-[#ff5247] text-white flex items-center justify-center font-bold text-2xl shadow-lg active:scale-90 transition shrink-0"
                          title="Glance Action: Log Smoked"
                        >
                          +
                        </button>
                        <button
                          onClick={logSkippedOne}
                          className={`w-7 h-7 rounded-full flex items-center justify-center text-xs transition active:scale-90 ${
                            widgetTheme === 'white' ? 'bg-[#e4e4e7] text-[#0284c7]' : 'bg-[#1e1e24] text-[#38bdf8]'
                          }`}
                          title="Ghost Counter: Resisted!"
                        >
                          🛡️
                        </button>
                      </div>

                      {/* Right Stats with Dot-Matrix Flavor */}
                      <div
                        onClick={() => {
                          setPhoneScreen('home');
                          setSnackbar("Glance Action: Opened SmokeLedger");
                        }}
                        className="flex-1 cursor-pointer font-mono"
                      >
                        <div className="flex items-center justify-between text-xs py-0.5">
                          <span className={widgetTheme === 'white' ? 'text-[#52525b]' : 'text-[#a1a1aa]'}>Today:</span>
                          <span className="font-bold">
                            {todaySmoked} cigs {todaySkipped > 0 && <span className="text-[#0284c7]">({todaySkipped} resisted)</span>}
                          </span>
                        </div>
                        <div className="flex items-center justify-between text-xs py-0.5">
                          <span className={widgetTheme === 'white' ? 'text-[#52525b]' : 'text-[#a1a1aa]'}>Week:</span>
                          <span className={widgetTheme === 'white' ? 'text-[#52525b]' : 'text-[#a1a1aa]'}>
                            {weekSmoked} cigs · {formatUnits(weekSpent)} u
                          </span>
                        </div>
                        <div className="flex items-center justify-between text-xs py-0.5">
                          <span className={widgetTheme === 'white' ? 'text-[#52525b]' : 'text-[#a1a1aa]'}>Pace:</span>
                          <span className="text-[#ff3b30] font-semibold">
                            ≈ {mirrorYearlyCigs}/yr · {formatUnits(mirrorYearlyUnits)} u
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Bottom strip */}
                    <div className={`mt-3 rounded-lg px-2.5 py-1.5 flex items-center justify-between text-[11px] ${
                      widgetTheme === 'white' ? 'bg-[#e4e4e7]' : 'bg-[#1e1e24]'
                    }`}>
                      <span className={widgetTheme === 'white' ? 'text-[#71717a]' : 'text-[#a1a1aa]'}>Could save today:</span>
                      <span className="font-bold text-[#ff3b30]">{formatUnits(todaySpent)} units</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* 9 Features Overview Card */}
              <div className="bg-[#121216] border border-[#202026] rounded-3xl p-6 text-left space-y-3">
                <h4 className="font-bold text-white text-sm">9 Features Integrated in SmokeLedger</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#38bdf8] font-bold block">1. Ghost Counter</span>
                    <span className="text-[#a1a1aa]">Log cravings defeated (+saved units)</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#ff6e40] font-bold block">2. Damage Report</span>
                    <span className="text-[#a1a1aa]">Clinical 30-day PDF export for doctor</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#f59e0b] font-bold block">3. The Mirror Pace</span>
                    <span className="text-[#a1a1aa]">Long-term shock (yearly pace multiplier)</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#22c55e] font-bold block">4. Live Smoke Timer</span>
                    <span className="text-[#a1a1aa]">Real-time smoke-free duration clock</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#ec4899] font-bold block">5. Edit Past Days</span>
                    <span className="text-[#a1a1aa]">Fix yesterday's forgot-to-log entries</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#a78bfa] font-bold block">6. Wishlist Goal</span>
                    <span className="text-[#a1a1aa]">"Headphones in 34 days" motivation</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-white font-bold block">7. Nothing OS Theme</span>
                    <span className="text-[#a1a1aa]">NDot Dark + Light monochrome widgets</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e]">
                    <span className="text-[#eab308] font-bold block">8. 9 PM Summary</span>
                    <span className="text-[#a1a1aa]">Offline WorkManager evening notification</span>
                  </div>
                  <div className="bg-[#18181c] p-2.5 rounded-xl border border-[#26262e] col-span-2">
                    <span className="text-[#10b981] font-bold block">9. PIN Lock + Local JSON Backup</span>
                    <span className="text-[#a1a1aa]">Roommate privacy &amp; zero-cloud export/import</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: DAMAGE REPORT PDF */}
        {activeTab === 'damage_report' && (
          <div className="max-w-4xl mx-auto text-left space-y-6">
            <div className="bg-[#121216] border border-[#202026] rounded-3xl p-6">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h3 className="font-bold text-white text-lg flex items-center gap-2">
                    <FileText className="w-5 h-5 text-[#ff6e40]" />
                    "Damage Report" — Clinical Export for Doctor / Therapist
                  </h3>
                  <p className="text-xs text-[#a1a1aa] mt-0.5">
                    Generates a formal, printable medical report with 30-day totals, Ghost resistances, and pace projections.
                  </p>
                </div>
                <button
                  onClick={() => setShowDamageReportModal(true)}
                  className="bg-[#ff6e40] hover:bg-[#ff8a65] text-white px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 transition"
                >
                  <FileText className="w-4 h-4" />
                  Preview &amp; Print PDF
                </button>
              </div>

              {/* REPORT PREVIEW */}
              <div className="bg-white text-black p-8 rounded-2xl shadow-2xl font-serif text-xs space-y-6 max-w-2xl mx-auto border border-gray-300">
                <div className="border-b-2 border-black pb-3">
                  <h2 className="text-xl font-bold font-sans tracking-tight">SmokeLedger — Clinical Damage Report</h2>
                  <div className="text-[11px] text-gray-600 font-sans mt-0.5">
                    Generated: {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })} • Confidential Patient Self-Report
                  </div>
                </div>

                {/* 30-Day Metrics Box */}
                <div className="border border-gray-400 p-4 rounded space-y-2">
                  <div className="font-sans font-bold text-xs uppercase tracking-wider text-red-600">
                    30-Day Behaviour Profile
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs font-sans">
                    <div>
                      <span className="text-gray-600">Total Smoked (30 Days):</span>
                      <div className="font-bold text-base text-black">{monthSmoked} cigarettes</div>
                    </div>
                    <div>
                      <span className="text-gray-600">Daily Average:</span>
                      <div className="font-bold text-base text-black">{(monthSmoked / 30).toFixed(1)} cigs / day</div>
                    </div>
                    <div>
                      <span className="text-gray-600">Financial Impact:</span>
                      <div className="font-bold text-base text-black">{formatUnits(monthSpent)} units spent</div>
                    </div>
                    <div>
                      <span className="text-gray-600">Craving Resistances:</span>
                      <div className="font-bold text-base text-blue-700">{skippedLogs.length} avoided (Ghost Counter)</div>
                    </div>
                  </div>
                </div>

                {/* The Mirror */}
                <div className="space-y-1.5 font-sans">
                  <div className="font-bold text-xs uppercase tracking-wider text-red-600">
                    The Mirror — 1-Year Pace Projection
                  </div>
                  <p className="text-[11px] text-gray-700">
                    If current habits continue without intervention over the next 12 months:
                  </p>
                  <ul className="list-disc list-inside text-xs space-y-1 text-gray-800">
                    <li>Projected annual consumption: <strong>{mirrorYearlyCigs} cigarettes</strong></li>
                    <li>Projected financial expenditure: <strong>{formatUnits(mirrorYearlyUnits)} units</strong></li>
                    <li>Estimated toxic load: <strong>Approx. {monthSmoked * 11} mg tar / month</strong></li>
                  </ul>
                </div>

                {/* Clinical Notes Section */}
                <div className="pt-2 border-t border-gray-300 font-sans space-y-2">
                  <div className="font-bold text-xs uppercase text-gray-700">Doctor / Therapist Clinical Notes</div>
                  <div className="border-b border-gray-300 h-6"></div>
                  <div className="border-b border-gray-300 h-6"></div>
                  <div className="border-b border-gray-300 h-6"></div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: WISHLIST & THE MIRROR */}
        {activeTab === 'wishlist' && (
          <div className="max-w-4xl mx-auto text-left space-y-6">
            <div className="bg-[#121216] border border-[#202026] rounded-3xl p-6">
              <h3 className="font-bold text-white text-lg flex items-center gap-2 mb-2">
                <Gift className="w-5 h-5 text-[#a78bfa]" />
                Wishlist Goal Calculator &amp; The Mirror Pace
              </h3>
              <p className="text-xs text-[#a1a1aa] mb-6">
                Turn abstract guilt into concrete dreams. Configure your goal item and see how small reductions yield rapid rewards.
              </p>

              {/* Wishlist Inputs */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                <div className="bg-[#18181c] p-4 rounded-2xl border border-[#26262e] space-y-2">
                  <label className="text-xs text-[#a1a1aa]">Your Dream Item</label>
                  <input
                    type="text"
                    value={wishlistTitle}
                    onChange={(e) => setWishlistTitle(e.target.value)}
                    className="w-full bg-[#121216] border border-[#2a2a34] rounded-xl px-3 py-2 text-sm text-white"
                  />
                </div>
                <div className="bg-[#18181c] p-4 rounded-2xl border border-[#26262e] space-y-2">
                  <label className="text-xs text-[#a1a1aa]">Target Cost (Units)</label>
                  <input
                    type="number"
                    value={wishlistCost}
                    onChange={(e) => setWishlistCost(Math.max(1, Number(e.target.value)))}
                    className="w-full bg-[#121216] border border-[#2a2a34] rounded-xl px-3 py-2 text-sm text-white"
                  />
                </div>
              </div>

              {/* Goal Timeline Breakdown */}
              <div className="bg-[#18181c] p-5 rounded-2xl border border-[#26262e] space-y-3">
                <h4 className="font-semibold text-white text-sm">How Fast You Reach "{wishlistTitle}":</h4>
                <div className="space-y-2 text-xs">
                  <div className="flex justify-between items-center p-3 rounded-xl bg-[#202028]">
                    <span className="text-[#d4d4d8]">Smoke <strong>1 less per day</strong>:</span>
                    <span className="font-mono font-bold text-[#a78bfa]">
                      Reach goal in {Math.ceil(wishlistCost / pricePerUnit)} days
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 rounded-xl bg-[#202028] border border-[#a78bfa]/40">
                    <span className="text-white font-semibold">Smoke <strong>2 less per day</strong>:</span>
                    <span className="font-mono font-bold text-[#a78bfa] text-sm">
                      Reach goal in {wishlistDays} days!
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 rounded-xl bg-[#202028]">
                    <span className="text-[#d4d4d8]">Quit completely today:</span>
                    <span className="font-mono font-bold text-[#22c55e]">
                      Reach goal in {Math.ceil(wishlistCost / (Math.max(1, weekDailyAvg) * pricePerUnit))} days
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: FILE EXPLORER */}
        {activeTab === 'files' && (
          <div className="grid grid-cols-1 md:grid-cols-12 gap-6 text-left">
            <div className="md:col-span-5 bg-[#121216] border border-[#202026] rounded-3xl p-4 flex flex-col h-[740px]">
              <div className="relative mb-3">
                <Search className="w-4 h-4 text-[#71717a] absolute left-3 top-2.5" />
                <input
                  type="text"
                  value={fileSearch}
                  onChange={(e) => setFileSearch(e.target.value)}
                  placeholder="Search files (e.g. DamageReport, CigWidget)..."
                  className="w-full bg-[#1c1c21] border border-[#2a2a32] rounded-xl pl-9 pr-3 py-2 text-xs text-white placeholder-[#71717a] focus:outline-none focus:border-[#ff6e40]"
                />
              </div>

              <div className="flex flex-wrap gap-1.5 mb-3 pb-2 border-b border-[#202026]">
                {['all', 'kotlin', 'gradle', 'manifest', 'res', 'ci', 'docs'].map(cat => (
                  <button
                    key={cat}
                    onClick={() => setCategoryFilter(cat)}
                    className={`px-2.5 py-1 rounded-lg text-[11px] font-medium transition capitalize ${
                      categoryFilter === cat ? 'bg-[#ff6e40] text-white' : 'bg-[#1e1e24] text-[#a1a1aa] hover:text-white'
                    }`}
                  >
                    {cat}
                  </button>
                ))}
              </div>

              <div className="flex-1 overflow-y-auto space-y-1 pr-1">
                {filteredFiles.map(file => (
                  <button
                    key={file.path}
                    onClick={() => setSelectedFile(file)}
                    className={`w-full text-left p-2.5 rounded-xl border transition flex items-start gap-2.5 ${
                      selectedFile.path === file.path
                        ? 'bg-[#ff6e40]/10 border-[#ff6e40]/50 text-white'
                        : 'bg-[#18181b]/50 border-transparent hover:border-[#27272a] text-[#a1a1aa] hover:text-[#ededed]'
                    }`}
                  >
                    <FileCode className={`w-4 h-4 shrink-0 mt-0.5 ${selectedFile.path === file.path ? 'text-[#ff6e40]' : 'text-[#71717a]'}`} />
                    <div className="min-w-0 flex-1">
                      <div className="font-mono text-xs truncate font-medium">{file.path}</div>
                      <div className="text-[10px] text-[#71717a] truncate mt-0.5">{file.description}</div>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            <div className="md:col-span-7 bg-[#121216] border border-[#202026] rounded-3xl p-5 flex flex-col h-[740px]">
              <div className="flex items-center justify-between pb-3 border-b border-[#202026] mb-3">
                <div>
                  <div className="font-mono text-sm font-semibold text-white">{selectedFile.path}</div>
                  <div className="text-xs text-[#a1a1aa]">{selectedFile.description}</div>
                </div>
                <button
                  onClick={() => handleCopyCode(selectedFile.content)}
                  className="flex items-center gap-1.5 bg-[#27272a] hover:bg-[#3f3f46] text-white px-3 py-1.5 rounded-xl text-xs font-medium transition active:scale-95"
                >
                  {copiedFile ? <Check className="w-3.5 h-3.5 text-[#4ade80]" /> : <Copy className="w-3.5 h-3.5" />}
                  {copiedFile ? "Copied!" : "Copy Code"}
                </button>
              </div>

              <pre className="flex-1 overflow-auto bg-[#0a0a0d] p-4 rounded-2xl border border-[#202026] font-mono text-[11.5px] leading-relaxed text-[#ededed] select-all whitespace-pre">
                {selectedFile.content}
              </pre>
            </div>
          </div>
        )}

        {/* TAB 5: INSTALL GUIDE */}
        {activeTab === 'guide' && (
          <div className="max-w-4xl mx-auto text-left space-y-6">
            <div className="bg-[#121216] border border-[#202026] rounded-3xl p-6">
              <h3 className="font-bold text-white text-lg flex items-center gap-2 mb-3">
                <Smartphone className="w-5 h-5 text-[#ff6e40]" />
                How to Build &amp; Install SmokeLedger on Android
              </h3>
              <div className="space-y-3 text-xs text-[#a1a1aa] leading-relaxed">
                <div className="flex gap-3 items-start bg-[#18181c] p-3.5 rounded-2xl border border-[#26262e]">
                  <span className="w-5 h-5 rounded-full bg-[#ff6e40] text-white font-bold flex items-center justify-center shrink-0 text-xs">1</span>
                  <div>
                    <strong className="text-white">Push to GitHub:</strong>
                    <p className="mt-0.5">Push this repo to GitHub. The included workflow (<code className="text-[#ededed]">.github/workflows/build.yml</code>) builds your debug APK automatically on push.</p>
                  </div>
                </div>

                <div className="flex gap-3 items-start bg-[#18181c] p-3.5 rounded-2xl border border-[#26262e]">
                  <span className="w-5 h-5 rounded-full bg-[#ff6e40] text-white font-bold flex items-center justify-center shrink-0 text-xs">2</span>
                  <div>
                    <strong className="text-white">Download APK Artifact:</strong>
                    <p className="mt-0.5">Under GitHub Actions, download the APK from the latest build artifact onto your phone.</p>
                  </div>
                </div>

                <div className="flex gap-3 items-start bg-[#18181c] p-3.5 rounded-2xl border border-[#26262e]">
                  <span className="w-5 h-5 rounded-full bg-[#ff6e40] text-white font-bold flex items-center justify-center shrink-0 text-xs">3</span>
                  <div>
                    <strong className="text-white">Add Nothing-Style Widget:</strong>
                    <p className="mt-0.5">Long-press your home screen, tap <strong>Widgets</strong>, find <strong>SmokeLedger</strong>, and place the NDot widget on your screen!</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* MODAL: EDIT PAST DAYS */}
      {showEditPastDaysModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-6 z-50">
          <div className="bg-[#18181c] border border-[#26262e] rounded-3xl p-6 max-w-sm w-full text-left space-y-4 shadow-2xl">
            <h4 className="font-bold text-white text-base flex items-center gap-2">
              <Calendar className="w-5 h-5 text-[#ff6e40]" />
              Edit Past Day's Log
            </h4>
            <p className="text-xs text-[#a1a1aa]">
              Forgot to log yesterday or earlier? Add missing cigarettes so your data stays true:
            </p>

            <div className="space-y-2">
              <button
                onClick={() => addPastDayLogs(1, 1, false)}
                className="w-full py-2 bg-[#202028] hover:bg-[#282834] text-white text-xs font-medium rounded-xl border border-[#2e2e3a] transition"
              >
                +1 Cigarette to Yesterday
              </button>
              <button
                onClick={() => addPastDayLogs(1, 2, false)}
                className="w-full py-2 bg-[#202028] hover:bg-[#282834] text-white text-xs font-medium rounded-xl border border-[#2e2e3a] transition"
              >
                +2 Cigarettes to Yesterday
              </button>
              <button
                onClick={() => addPastDayLogs(2, 1, false)}
                className="w-full py-2 bg-[#202028] hover:bg-[#282834] text-white text-xs font-medium rounded-xl border border-[#2e2e3a] transition"
              >
                +1 Cigarette to 2 Days Ago
              </button>
            </div>

            <button
              onClick={() => setShowEditPastDaysModal(false)}
              className="w-full py-2 bg-[#27272a] text-white text-xs rounded-xl"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* MODAL: DAMAGE REPORT PDF PREVIEW */}
      {showDamageReportModal && (
        <div className="fixed inset-0 bg-black/85 backdrop-blur-md flex items-center justify-center p-6 z-50">
          <div className="bg-[#18181c] border border-[#26262e] rounded-3xl p-6 max-w-lg w-full text-left space-y-4 shadow-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <h4 className="font-bold text-white text-base flex items-center gap-2">
                <FileText className="w-5 h-5 text-[#ff6e40]" />
                Damage Report Preview (PDF)
              </h4>
              <button onClick={() => setShowDamageReportModal(false)} className="text-[#a1a1aa] hover:text-white">✕</button>
            </div>

            <div className="bg-white text-black p-5 rounded-xl font-sans text-xs space-y-3">
              <div className="border-b border-black pb-2">
                <div className="font-bold text-sm">SmokeLedger — Clinical Damage Report</div>
                <div className="text-[10px] text-gray-600">Generated: {new Date().toLocaleDateString()}</div>
              </div>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <div>30-Day Smoked: <strong>{monthSmoked} cigs</strong></div>
                <div>Daily Average: <strong>{(monthSmoked / 30).toFixed(1)}/day</strong></div>
                <div>Units Spent: <strong>{formatUnits(monthSpent)} units</strong></div>
                <div>Ghost Resisted: <strong>{skippedLogs.length} avoided</strong></div>
              </div>
              <div className="pt-2 border-t border-gray-200 text-xs">
                <div>Annual Pace: <strong>{mirrorYearlyCigs} cigs ({formatUnits(mirrorYearlyUnits)} units)</strong></div>
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => {
                  window.print();
                  setSnackbar("Opening print dialogue for PDF generation");
                }}
                className="flex-1 py-2 bg-[#ff6e40] text-white text-xs font-semibold rounded-xl"
              >
                Print / Save as PDF
              </button>
              <button
                onClick={() => setShowDamageReportModal(false)}
                className="px-4 py-2 bg-[#27272a] text-white text-xs rounded-xl"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: LOCAL JSON BACKUP */}
      {showBackupModal && (
        <div className="fixed inset-0 bg-black/85 backdrop-blur-md flex items-center justify-center p-6 z-50">
          <div className="bg-[#18181c] border border-[#26262e] rounded-3xl p-6 max-w-lg w-full text-left space-y-4 shadow-2xl">
            <div className="flex items-center justify-between">
              <h4 className="font-bold text-white text-base">Local JSON Backup File</h4>
              <button onClick={() => setShowBackupModal(false)} className="text-[#a1a1aa] hover:text-white">✕</button>
            </div>
            <p className="text-xs text-[#a1a1aa]">
              This file contains all your logged rows and settings. Save it securely on your device:
            </p>
            <textarea
              readOnly
              value={backupJsonString}
              className="w-full h-44 bg-[#0d0d10] border border-[#2a2a32] rounded-xl p-3 font-mono text-[11px] text-[#ededed] resize-none"
            />
            <div className="flex gap-2">
              <button
                onClick={() => {
                  const blob = new Blob([backupJsonString], { type: 'application/json' });
                  const url = URL.createObjectURL(blob);
                  const a = document.createElement('a');
                  a.href = url;
                  a.download = `smokeledger_backup_${new Date().toISOString().slice(0, 10)}.json`;
                  document.body.appendChild(a);
                  a.click();
                  document.body.removeChild(a);
                  URL.revokeObjectURL(url);
                  setSnackbar("Downloaded smokeledger_backup.json!");
                }}
                className="flex-1 py-2 bg-[#38bdf8] text-black font-semibold text-xs rounded-xl"
              >
                Download JSON File
              </button>
              <button
                onClick={() => setShowBackupModal(false)}
                className="px-4 py-2 bg-[#27272a] text-white text-xs rounded-xl"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: DONATE & SCHIZOPHRENIA AWARENESS */}
      {showDonateModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-6 z-50">
          <div className="bg-[#18181c] border border-[#26262e] rounded-3xl p-6 max-w-md w-full text-left space-y-4 shadow-2xl">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-[#f59e0b]">
                <Heart className="w-5 h-5 fill-[#f59e0b]" />
                <h4 className="font-bold text-white text-base">Support Developer &amp; Awareness</h4>
              </div>
              <button onClick={() => setShowDonateModal(false)} className="text-[#a1a1aa] hover:text-white">✕</button>
            </div>

            <p className="text-xs text-[#a1a1aa] leading-relaxed">
              Donate directly to developer Puneet towards open-source app maintenance and an individual awareness campaign towards schizophrenia.
            </p>

            <div className="bg-[#202028] p-4 rounded-2xl border border-[#2a2a34] space-y-2">
              <div className="text-[11px] text-[#71717a]">UPI ID (PhonePe / GPay / Paytm / BHIM)</div>
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-base text-[#f59e0b]">puneetfsn@ybl</span>
                <button
                  onClick={copyUpiId}
                  className="flex items-center gap-1.5 bg-[#f59e0b] hover:bg-[#fbbf24] text-black font-semibold px-3 py-1.5 rounded-xl text-xs transition"
                >
                  <Copy className="w-3.5 h-3.5" />
                  {copiedUpi ? "Copied!" : "Copy UPI"}
                </button>
              </div>
            </div>

            <div className="bg-[#202028]/70 p-3 rounded-2xl border border-[#2a2a34] text-[11px] text-[#a1a1aa]">
              🧠 <strong>Schizophrenia Awareness:</strong> Early intervention, empathy, and open dialogue reduce trauma and restore lives.
            </div>

            <button
              onClick={() => setShowDonateModal(false)}
              className="w-full py-2 bg-[#27272a] text-white text-xs rounded-xl"
            >
              Close
            </button>
          </div>
        </div>
      )}

      {/* MODAL: DELETE ALL DATA */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-6 z-50">
          <div className="bg-[#18181c] border border-[#26262e] rounded-3xl p-6 max-w-sm w-full text-left space-y-4 shadow-2xl">
            <h4 className="font-bold text-white text-base">Delete all data?</h4>
            <p className="text-xs text-[#a1a1aa]">
              Permanently erases all logged smoked and skipped cigarettes from local storage.
            </p>
            <div className="flex gap-2 justify-end">
              <button onClick={() => setShowDeleteModal(false)} className="px-3 py-1.5 text-xs text-[#a1a1aa]">
                Cancel
              </button>
              <button onClick={deleteAll} className="px-3.5 py-1.5 bg-[#f87171] text-white text-xs font-semibold rounded-xl">
                Delete Everything
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
