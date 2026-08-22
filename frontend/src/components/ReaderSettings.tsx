"use client";

import { RotateCcw, WifiOff, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  NativeSelect,
  NativeSelectOption,
} from "@/components/ui/native-select";

export type ReaderColorPreset = "dark" | "light" | "gruvbox";

export type ReaderFontFamily =
  | "georgia"
  | "arial"
  | "courier"
  | "merriweather"
  | "lora"
  | "noto-serif";

export interface ReaderSettings {
  fontSize: number;
  fontWeight: number;
  lineHeight: number;
  colorPreset: ReaderColorPreset;
  fontFamily: ReaderFontFamily;
}

export const DEFAULT_READER_SETTINGS: ReaderSettings = {
  fontSize: 20,
  fontWeight: 500,
  lineHeight: 1.7,
  colorPreset: "dark",
  fontFamily: "georgia",
};

interface ReaderSettingsPanelProps {
  settings: ReaderSettings;
  isOnline: boolean;
  onChange: (settings: ReaderSettings) => void;
  onClose: () => void;
}

interface RangeSettingProps {
  id: string;
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  displayValue: string;
  onChange: (value: number) => void;
}

const RangeSetting = ({
  id,
  label,
  value,
  min,
  max,
  step,
  displayValue,
  onChange,
}: RangeSettingProps) => {
  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between gap-4">
        <label htmlFor={id} className="text-sm text-muted-foreground">
          {label}
        </label>

        <span className="text-sm font-semibold text-foreground">
          {displayValue}
        </span>
      </div>

      <input
        id={id}
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(event) => {
          onChange(Number(event.target.value));
        }}
        className="h-1.5 w-full cursor-pointer accent-primary"
      />
    </div>
  );
};

const colorPresets: Array<{
  value: ReaderColorPreset;
  label: string;
  background: string;
  foreground: string;
}> = [
  {
    value: "dark",
    label: "Dark",
    background: "#151515",
    foreground: "#f0eee8",
  },
  {
    value: "light",
    label: "Light",
    background: "#f4efe6",
    foreground: "#292522",
  },
  {
    value: "gruvbox",
    label: "Gruvbox",
    background: "#282828",
    foreground: "#ebdbb2",
  },
];

const fontOptions: Array<{
  value: ReaderFontFamily;
  label: string;
  onlineOnly?: boolean;
}> = [
  {
    value: "georgia",
    label: "Georgia",
  },
  {
    value: "arial",
    label: "Arial",
  },
  {
    value: "courier",
    label: "Courier New",
  },
  {
    value: "merriweather",
    label: "Merriweather",
    onlineOnly: true,
  },
  {
    value: "lora",
    label: "Lora",
    onlineOnly: true,
  },
  {
    value: "noto-serif",
    label: "Noto Serif",
    onlineOnly: true,
  },
];

export const ReaderSetting = ({
  settings,
  isOnline,
  onChange,
  onClose,
}: ReaderSettingsPanelProps) => {
  return (
    <div className="absolute inset-0 z-30 flex items-start justify-center px-4 pt-18 sm:pt-24">
      <button
        type="button"
        aria-label="Đóng cài đặt"
        className="absolute inset-0 bg-black/55"
        onClick={onClose}
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="reader-settings-title"
        className="relative z-10 max-h-[calc(100dvh-7rem)] w-full max-w-lg overflow-y-auto border border-border-strong bg-card shadow-2xl"
      >
        <header className="flex items-center justify-between border-b border-border px-5 py-4">
          <div>
            <h2
              id="reader-settings-title"
              className="text-lg font-semibold text-foreground"
            >
              Setting
            </h2>
          </div>

          <Button
            type="button"
            variant="ghost"
            size="icon-sm"
            aria-label="Đóng"
            onClick={onClose}
          >
            <X />
          </Button>
        </header>

        <div className="space-y-7 p-5">
          <RangeSetting
            id="reader-font-size"
            label="Cỡ chữ"
            value={settings.fontSize}
            min={14}
            max={30}
            step={1}
            displayValue={`${settings.fontSize}px`}
            onChange={(fontSize) => {
              onChange({ ...settings, fontSize });
            }}
          />

          <RangeSetting
            id="reader-font-weight"
            label="Độ đậm"
            value={settings.fontWeight}
            min={300}
            max={700}
            step={100}
            displayValue={String(settings.fontWeight)}
            onChange={(fontWeight) => {
              onChange({ ...settings, fontWeight });
            }}
          />

          <RangeSetting
            id="reader-line-height"
            label="Khoảng cách dòng"
            value={settings.lineHeight}
            min={1.3}
            max={2.2}
            step={0.1}
            displayValue={settings.lineHeight.toFixed(1)}
            onChange={(lineHeight) => {
              onChange({ ...settings, lineHeight });
            }}
          />

          <div className="space-y-3">
            <p className="text-sm text-muted-foreground">Màu đọc sách</p>

            <div className="grid grid-cols-3 gap-2">
              {colorPresets.map((preset) => (
                <button
                  key={preset.value}
                  type="button"
                  aria-pressed={settings.colorPreset === preset.value}
                  onClick={() => {
                    onChange({
                      ...settings,
                      colorPreset: preset.value,
                    });
                  }}
                  className="border border-border bg-field p-3 text-left transition hover:border-ring aria-pressed:border-primary aria-pressed:ring-1 aria-pressed:ring-primary"
                >
                  <span
                    className="mb-3 block h-8 border border-white/10"
                    style={{
                      backgroundColor: preset.background,
                      color: preset.foreground,
                    }}
                  />

                  <span className="text-xs font-semibold text-foreground">
                    {preset.label}
                  </span>
                </button>
              ))}
            </div>
          </div>

          <div className="space-y-3">
            <label
              htmlFor="reader-font-family"
              className="text-sm text-muted-foreground"
            >
              Font chữ
            </label>

            <NativeSelect
              id="reader-font-family"
              value={settings.fontFamily}
              onChange={(event) => {
                onChange({
                  ...settings,
                  fontFamily: event.target.value as ReaderFontFamily,
                });
              }}
            >
              {fontOptions.map((font) => (
                <NativeSelectOption
                  key={font.value}
                  value={font.value}
                  disabled={font.onlineOnly && !isOnline}
                >
                  {font.label}
                </NativeSelectOption>
              ))}
            </NativeSelect>

            {!isOnline && (
              <p className="flex items-center gap-2 text-xs text-muted-foreground">
                <WifiOff className="size-3.5" />
                Google Fonts bị tắt khi đang offline.
              </p>
            )}
          </div>
        </div>

        <footer className="flex justify-end border-t border-border px-5 py-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              onChange(DEFAULT_READER_SETTINGS);
            }}
          >
            <RotateCcw data-icon="inline-start" />
            Khôi phục mặc định
          </Button>
        </footer>
      </div>
    </div>
  );
};
