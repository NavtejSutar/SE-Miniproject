import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

export default function MarkdownMessage({ content }) {
  if (!content) return null;

  // Pre-process content:
  // 1. Strip any legacy "### Source: ..."
  let clean = content.replace(/###\s*Source:?\s*\[?[^\]\n]*\]?/gi, '').trim();

  // 2. Format common LaTeX wrappers \[ ... \] and \( ... \) into clean markdown math representation
  clean = clean
    .replace(/\\\[\s*([\s\S]*?)\s*\\\]/g, '\n```math\n$1\n```\n')
    .replace(/\\\(\s*([\s\S]*?)\s*\\\)/g, ' `$1` ');

  return (
    <div className="markdown-content text-gray-200 text-sm leading-relaxed space-y-3 font-sans select-text">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          h1: ({ children }) => (
            <h1 className="text-xl md:text-2xl font-black text-white uppercase tracking-wide mt-5 mb-2 pb-1 border-b border-[#2a2d39]">
              {children}
            </h1>
          ),
          h2: ({ children }) => (
            <h2 className="text-lg md:text-xl font-bold text-white tracking-wide mt-4 mb-2 flex items-center gap-2">
              <span className="w-1.5 h-4 bg-[#ff3838] inline-block"></span>
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3 className="text-base font-bold text-white tracking-wide mt-4 mb-1.5 text-[#ff4c4c]">
              {children}
            </h3>
          ),
          h4: ({ children }) => (
            <h4 className="text-sm font-bold text-gray-100 uppercase tracking-wider mt-3 mb-1">
              {children}
            </h4>
          ),
          p: ({ children }) => (
            <p className="mb-2 leading-relaxed text-gray-200">{children}</p>
          ),
          ul: ({ children }) => (
            <ul className="list-disc pl-5 space-y-1.5 my-2.5 text-gray-300">
              {children}
            </ul>
          ),
          ol: ({ children }) => (
            <ol className="list-decimal pl-5 space-y-2 my-2.5 text-gray-300 font-mono text-xs">
              {children}
            </ol>
          ),
          li: ({ children }) => (
            <li className="leading-relaxed pl-1">{children}</li>
          ),
          strong: ({ children }) => (
            <strong className="font-bold text-white">{children}</strong>
          ),
          em: ({ children }) => (
            <em className="text-gray-300 italic">{children}</em>
          ),
          blockquote: ({ children }) => (
            <blockquote className="border-l-2 border-[#ff3838] pl-3.5 py-1 my-3 bg-[#151720] text-gray-300 italic">
              {children}
            </blockquote>
          ),
          code: ({ inline, className, children, ...props }) => {
            const isMath = className === 'language-math';
            if (isMath) {
              return (
                <div className="my-3 p-3.5 bg-[#14161f] border border-[#2c303f] font-mono text-sm text-amber-200 overflow-x-auto text-center rounded-none shadow-inner">
                  {children}
                </div>
              );
            }
            if (inline) {
              return (
                <code className="px-1.5 py-0.5 bg-[#181a23] border border-[#2b2f3d] text-amber-300 font-mono text-xs font-medium">
                  {children}
                </code>
              );
            }
            return (
              <pre className="p-4 bg-[#11131a] border border-[#282c39] overflow-x-auto my-3 text-xs font-mono text-emerald-300">
                <code>{children}</code>
              </pre>
            );
          },
          table: ({ children }) => (
            <div className="overflow-x-auto my-3">
              <table className="w-full text-left text-xs font-mono border border-[#2b2e3c]">
                {children}
              </table>
            </div>
          ),
          thead: ({ children }) => (
            <thead className="bg-[#171922] border-b border-[#2b2e3c] text-white uppercase text-[10px]">
              {children}
            </thead>
          ),
          tbody: ({ children }) => (
            <tbody className="divide-y divide-[#222530]">{children}</tbody>
          ),
          tr: ({ children }) => (
            <tr className="hover:bg-[#151720]">{children}</tr>
          ),
          th: ({ children }) => <th className="p-2.5 font-bold">{children}</th>,
          td: ({ children }) => <td className="p-2.5 text-gray-300">{children}</td>,
        }}
      >
        {clean}
      </ReactMarkdown>
    </div>
  );
}
