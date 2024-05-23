


fix

```text
link:
     [exec] /usr/bin/i686-w64-mingw32-ld: cannot find -lavformat
     [exec] /usr/bin/i686-w64-mingw32-ld: cannot find -lavcodec
     [exec] /usr/bin/i686-w64-mingw32-ld: cannot find -lavutil
     [exec] /usr/bin/i686-w64-mingw32-ld: cannot find -lswscale
     [exec] /usr/bin/i686-w64-mingw32-ld: cannot find -lswresample
     [exec] collect2: error: ld returned 1 exit status
```
说明ant xml文件对应不上


```text

Couldn't load shared library 'arc-video-desktop64.dll' for target: xxx(example:Windows 10, 64-bit)
```
说明你没有完成（正常完成）编译工作，请仔细检查编译步骤