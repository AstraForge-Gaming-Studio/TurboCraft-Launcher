package io.github.juanluci.starling;

import java.io.File;

/** NativeActivity and its helper processes share this private, TCL-only directory. */
final class RustStorage {
    private RustStorage() {}

    static File root(File appFiles) {
        File root = new File(appFiles, "rust-edition");
        if (!root.isDirectory() && !root.mkdirs() && !root.isDirectory()) {
            throw new IllegalStateException("Cannot create Rust edition storage");
        }
        return root;
    }
}
