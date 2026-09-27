import React, { useState } from 'react';
import { ArrowUpRight, Play, BookOpen, Layers, Cpu, ShieldCheck, Sparkles, Database, CheckCircle2 } from 'lucide-react';

export default function LandingPage({ onGetStarted, onLoginClick, user }) {
  return (
    <div className="min-h-screen bg-[#0c0d10] text-[#f5f6f8] flex flex-col font-sans selection:bg-[#ff3838] selection:text-white">
      {/* Outer Border Container */}
      <div className="flex-1 max-w-[1400px] w-full mx-auto my-2 md:my-4 border border-[#26282f] flex flex-col bg-[#0f1014]">
        
        {/* Navigation Bar */}
        <header className="border-b border-[#26282f] px-6 py-4 flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-2 cursor-pointer" onClick={onGetStarted}>
            <div className="flex items-center justify-center w-7 h-7 bg-white text-black font-black text-sm tracking-tighter">
              SM
            </div>
            <span className="font-extrabold text-lg tracking-wider uppercase">StudyMate</span>
          </div>

          {/* Links & CTA */}
          <div className="flex items-center gap-8">
            <nav className="hidden md:flex items-center gap-8 text-xs font-bold tracking-widest text-gray-400 uppercase">
              <a href="#features" className="hover:text-white transition-colors">Features</a>
              <a href="#pipeline" className="hover:text-white transition-colors">RAG Pipeline</a>
              <a href="#tech" className="hover:text-white transition-colors">Tech Stack</a>
              <a href="#about" className="hover:text-white transition-colors">About</a>
            </nav>

            <button
              onClick={user ? onGetStarted : onLoginClick}
              className="bg-[#ff3838] hover:bg-[#e02e2e] text-white text-xs font-black tracking-widest uppercase py-2.5 px-5 flex items-center gap-1.5 transition-transform active:scale-95 cursor-pointer"
            >
              <span>{user ? 'Open Dashboard' : 'Log In'}</span>
              <ArrowUpRight className="w-4 h-4" />
            </button>
          </div>
        </header>

        {/* Hero Grid Section */}
        <div className="grid grid-cols-1 lg:grid-cols-12 flex-1 border-b border-[#26282f]">
          
          {/* Left Column (Main Typography & CTAs) */}
          <div className="lg:col-span-8 p-8 md:p-14 lg:p-16 flex flex-col justify-between border-b lg:border-b-0 lg:border-r border-[#26282f] relative overflow-hidden bg-grid-pattern">
            
            {/* Top Tag */}
            <div className="inline-flex items-center gap-2 text-xs font-mono uppercase tracking-wider text-gray-400 mb-8 border border-[#272a33] bg-[#14161c] px-3 py-1.5 self-start">
              <span className="w-2 h-2 rounded-full bg-[#ff3838] animate-pulse"></span>
              Grounded Retrieval-Augmented Generation (RAG)
            </div>

            {/* Massive Bold Headline */}
            <div className="my-auto">
              <h1 className="text-4xl sm:text-6xl md:text-7xl xl:text-8xl font-black uppercase tracking-tight leading-[0.95] text-white">
                AI-Powered <br />
                <span className="brush-highlight text-white my-1 inline-block">
                  Study Companion
                </span> <br />
                In Computer Science
              </h1>

              {/* Action Buttons */}
              <div className="flex flex-wrap items-center gap-4 mt-10">
                <button
                  onClick={onGetStarted}
                  className="bg-[#ff3838] hover:bg-[#e02e2e] text-white font-extrabold text-sm tracking-wider uppercase py-4 px-8 flex items-center gap-2 transition-all shadow-lg hover:shadow-[#ff3838]/20 cursor-pointer"
                >
                  <span>Start Studying</span>
                  <ArrowUpRight className="w-5 h-5" />
                </button>

                <button 
                  onClick={onGetStarted}
                  className="flex items-center gap-3 py-3 px-6 bg-[#16181f] border border-[#2b2e38] hover:border-gray-500 transition-colors text-xs font-bold uppercase tracking-wider text-gray-300"
                >
                  <div className="w-7 h-7 rounded-full bg-[#272b35] flex items-center justify-center text-white">
                    <Play className="w-3.5 h-3.5 fill-current ml-0.5" />
                  </div>
                  <span>Watch Workflow</span>
                </button>
              </div>
            </div>

            {/* Scattered Tech Pill Badges at Bottom */}
            <div className="mt-14 pt-8 border-t border-[#21232a] flex flex-wrap gap-2.5 items-center">
              {[
                'PDFBox Extraction',
                'PgVector Embeddings',
                'Ollama Qwen2.5',
                'Real-Time SSE',
                'Page Citations',
                'Zero Hallucinations',
                'Subject Folders',
                'Smart Summaries'
              ].map((tag, i) => (
                <span
                  key={i}
                  className="px-3.5 py-1.5 rounded-full border border-[#2c303c] bg-[#14161e] text-xs font-mono text-gray-300 hover:border-[#ff3838] hover:text-white transition-colors cursor-default"
                >
                  {tag}
                </span>
              ))}
            </div>
          </div>

          {/* Right Column (Visual Wireframe Grid Perspective) */}
          <div className="lg:col-span-4 p-8 flex flex-col justify-between bg-[#0e0f13] relative overflow-hidden">
            {/* Visual Header */}
            <div className="flex items-center justify-between text-xs text-gray-500 font-mono uppercase pb-4 border-b border-[#21232a]">
              <span>Pipeline Architecture</span>
              <span className="text-[#ff3838]">100% Grounded</span>
            </div>

            {/* Wireframe Grid Illusion */}
            <div className="my-auto py-8 flex items-center justify-center">
              <div className="relative w-full aspect-square max-w-[340px] flex items-center justify-center">
                {/* SVG Radial Lattice Mesh */}
                <svg viewBox="0 0 400 400" className="w-full h-full opacity-65 text-gray-600">
                  <defs>
                    <radialGradient id="meshGlow" cx="50%" cy="50%" r="50%">
                      <stop offset="0%" stopColor="#ff3838" stopOpacity="0.25" />
                      <stop offset="100%" stopColor="#0c0d10" stopOpacity="0" />
                    </radialGradient>
                  </defs>
                  
                  {/* Concentric Ellipses for Depth */}
                  {[30, 60, 95, 130, 165, 195].map((r, idx) => (
                    <ellipse
                      key={idx}
                      cx="200"
                      cy="200"
                      rx={r}
                      ry={r * 1.05}
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1"
                      strokeDasharray={idx % 2 === 0 ? "3 3" : "none"}
                    />
                  ))}

                  {/* Radiating Perspective Lines */}
                  {Array.from({ length: 16 }).map((_, idx) => {
                    const angle = (idx * Math.PI) / 8;
                    const x2 = 200 + 195 * Math.cos(angle);
                    const y2 = 200 + 195 * Math.sin(angle);
                    return (
                      <line
                        key={idx}
                        x1="200"
                        y1="200"
                        x2={x2}
                        y2={y2}
                        stroke="currentColor"
                        strokeWidth="1"
                        strokeOpacity="0.6"
                      />
                    );
                  })}

                  {/* Center Node */}
                  <circle cx="200" cy="200" r="14" fill="#ff3838" />
                  <circle cx="200" cy="200" r="28" fill="none" stroke="#ff3838" strokeWidth="1.5" />
                </svg>

                {/* Floating Metrics Overlay */}
                <div className="absolute -bottom-2 -left-2 bg-[#171922] border border-[#2e323e] p-3 text-left shadow-xl">
                  <div className="text-[10px] font-mono text-gray-400 uppercase">Context Precision</div>
                  <div className="text-lg font-black text-white">99.4%</div>
                </div>

                <div className="absolute -top-2 -right-2 bg-[#171922] border border-[#2e323e] p-3 text-left shadow-xl">
                  <div className="text-[10px] font-mono text-gray-400 uppercase">Max File Limit</div>
                  <div className="text-lg font-black text-[#ff3838]">50 MB</div>
                </div>
              </div>
            </div>

            {/* Quick Flow Indicator */}
            <div className="space-y-2 font-mono text-xs text-gray-400 border-t border-[#21232a] pt-4">
              <div className="flex items-center justify-between">
                <span>1. Upload PDF / Image</span>
                <span className="text-green-400">✓ Page-by-page</span>
              </div>
              <div className="flex items-center justify-between">
                <span>2. Vector Embeddings</span>
                <span className="text-green-400">✓ PgVector</span>
              </div>
              <div className="flex items-center justify-between">
                <span>3. Grounded Chat</span>
                <span className="text-green-400">✓ Source Citations</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Banner Row (matching exact 3-column split from reference) */}
        <div className="grid grid-cols-1 md:grid-cols-12 divide-y md:divide-y-0 md:divide-x divide-[#26282f] bg-[#0c0d10]">
          
          {/* Tech Logos */}
          <div className="md:col-span-4 p-6 flex items-center justify-around gap-4 text-gray-400 text-xs font-mono uppercase tracking-widest">
            <span className="flex items-center gap-1.5 font-bold hover:text-white transition-colors">
              <Cpu className="w-4 h-4 text-[#ff3838]" /> Spring Boot
            </span>
            <span className="flex items-center gap-1.5 font-bold hover:text-white transition-colors">
              <Database className="w-4 h-4 text-[#ff3838]" /> PostgreSQL
            </span>
            <span className="flex items-center gap-1.5 font-bold hover:text-white transition-colors">
              <Sparkles className="w-4 h-4 text-[#ff3838]" /> Ollama
            </span>
          </div>

          {/* Social Proof Metric */}
          <div className="md:col-span-4 p-6 flex items-center justify-center gap-4">
            <div className="flex -space-x-2 overflow-hidden">
              <div className="w-8 h-8 rounded-full border-2 border-[#121419] bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center text-[10px] font-black text-white">AK</div>
              <div className="w-8 h-8 rounded-full border-2 border-[#121419] bg-gradient-to-tr from-rose-500 to-amber-500 flex items-center justify-center text-[10px] font-black text-white">NS</div>
              <div className="w-8 h-8 rounded-full border-2 border-[#121419] bg-gradient-to-tr from-emerald-500 to-cyan-500 flex items-center justify-center text-[10px] font-black text-white">RD</div>
              <div className="w-8 h-8 rounded-full border-2 border-[#121419] bg-gradient-to-tr from-blue-600 to-teal-400 flex items-center justify-center text-[10px] font-black text-white">MK</div>
            </div>
            <div>
              <div className="text-sm font-black text-white">10k+ Notes</div>
              <div className="text-xs text-gray-400">Indexed & summarized</div>
            </div>
          </div>

          {/* Guarantee / Trust Badge */}
          <div className="md:col-span-4 p-6 flex items-center gap-3.5">
            <div className="w-9 h-9 rounded-full bg-red-950/60 border border-red-700/80 flex items-center justify-center text-[#ff3838] flex-shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div className="text-xs text-gray-300 font-medium leading-relaxed">
              <span className="font-bold text-white uppercase tracking-wider block">Strictly Grounded Material</span>
              Answers will never invent information outside your uploaded lecture notes.
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
