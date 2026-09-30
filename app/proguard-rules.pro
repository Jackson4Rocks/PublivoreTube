# PublivoreTube-specific R8 rules.
# NewPipe Extractor uses reflection and a JavaScript runtime for parts of extraction.
-keep class org.schabi.newpipe.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter { *; }
-dontwarn org.mozilla.javascript.**
