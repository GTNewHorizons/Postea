package com.gtnewhorizons.postea.utility;

import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import com.gtnewhorizons.postea.Postea;
import com.gtnewhorizons.postea.api.IVersionedTransformer;

/// Runs versioned transformers over saved NBT and reports their failures.
public final class VersionedPass {

    private VersionedPass() {}

    /// One transformer's work on `root`, which holds data stamped at `stored`.
    @FunctionalInterface
    public interface Step {

        void apply(IVersionedTransformer transformer, NBTTagCompound root, int stored);
    }

    /// Runs `step` for every transformer whose stamp on `root` differs from its current version. The steps work on a
    /// copy of `root`, which is stamped with the current versions and replaces the contents of `root` once every step
    /// has succeeded. A failing step throws the [#failure] for `subjectName` and `subject`, and leaves `root` as read.
    ///
    /// @return a copy of the stamps `root` was read with, or null if unstamped
    public static NBTTagCompound runAtomically(NBTTagCompound root, Iterable<IVersionedTransformer> transformers,
        String subjectName, String subject, Step step) {
        NBTTagCompound stamps = VersionStamps.read(root);
        NBTTagCompound carried = stamps == null ? null : (NBTTagCompound) stamps.copy();
        NBTTagCompound working = null;
        for (IVersionedTransformer transformer : transformers) {
            int current = transformer.currentVersion();
            int stored = VersionStamps.stored(carried, transformer.key());
            if (stored == current) continue;
            VersionedTransformerLog.announce(transformer, stored, current);
            Postea.LOG
                .debug("{}: {} {} from version {} to {}", transformer.key(), subjectName, subject, stored, current);
            if (working == null) working = (NBTTagCompound) root.copy();
            try {
                step.apply(transformer, working, stored);
            } catch (Throwable t) {
                throw failure(t, transformer, stored, current, subjectName, subject);
            }
        }
        if (working != null) {
            VersionStamps.write(working, carried, transformers);
            replaceContents(root, working);
        }
        return carried;
    }

    /// The exception a failed transformer run throws. Its crash report description names the transformer, the subject
    /// and both versions.
    public static ReportedException failure(Throwable cause, IVersionedTransformer transformer, int stored, int current,
        String subjectName, String subject) {
        String description = String.format(
            "Running versioned transformer %s on %s %s from version %s to %d",
            transformer.key(),
            subjectName.toLowerCase(Locale.ROOT),
            subject,
            VersionedTransformerLog.versionName(stored),
            current);
        CrashReport report = CrashReport.makeCrashReport(cause, description);
        CrashReportCategory category = report.makeCategory("Postea versioned transformer");
        category.addCrashSection("Key", transformer.key());
        category.addCrashSection("Stored version", stored);
        category.addCrashSection("Current version", current);
        category.addCrashSection(subjectName, subject);
        return new ReportedException(report);
    }

    private static void replaceContents(NBTTagCompound target, NBTTagCompound source) {
        for (Object key : new ArrayList<Object>(target.func_150296_c())) target.removeTag((String) key);
        for (Object key : source.func_150296_c()) target.setTag((String) key, source.getTag((String) key));
    }
}
