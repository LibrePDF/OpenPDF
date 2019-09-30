package com.lowagie.examples.objects.chunk;

import com.lowagie.examples.AbstractRunner;

public class ChunkRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "chunk");

        Background.main(argsNew);
        ChunkColor.main(argsNew);
        EndOfLine.main(argsNew);
        Generic.main(argsNew);
        Glossary.main(argsNew);
        Hyphenation.main(argsNew);
        Lines.main(argsNew);
        Rendering.main(argsNew);
        Skew.main(argsNew);
        SplitChar.main(argsNew);
        SubSupScript.main(argsNew);
        Width.main(argsNew);
    }
}
