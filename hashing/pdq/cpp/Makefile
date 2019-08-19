# ================================================================
CXX=x86_64-w64-mingw32-g++ -g -std=c++11
CC=x86_64-w64-mingw32-gcc -g -std=c99
AR=x86_64-w64-mingw32-ar
IFLAGS=-I../.. -I. -I/usr/x86_64-w64-mingw32/include -I$(HOME)/mingw64/include
# -Wno-char-subscripts is because of CImg.h
WFLAGS=-Wall -Wno-char-subscripts -Werror
LFLAGS=-lm -L/usr/x86_64-w64-mingw32/lib -L$(HOME)/mingw64/lib# -L. -lpdq
CIMG=-Dcimg_use_png -Dcimg_use_jpeg -Dcimg_use_tiff -Dcimg_use_zlib -lpng -ljpeg -ltiff -lz

CCOPT=$(CXX) $(CFLAGS) $(IFLAGS) $(WFLAGS) -O3
COPT=$(CC) $(CFLAGS) $(IFLAGS) $(WFLAGS) -O3
CCDBG=$(CXX) -g $(CFLAGS) $(IFLAGS) $(WFLAGS)
CCSAN=clang -fsanitize=address -g $(CFLAGS) $(IFLAGS) $(WFLAGS)

# ================================================================
# Please replace CImg.h with your own system image-decoder and edit
# io/pdqio.cpp.
LIBHDRS=\
 common/pdqbasetypes.h \
 common/pdqhashtypes.h \
 io/hashio.h \
 common/pdqhamming.h \
 hashing/pdqhashing.h \
 downscaling/downscaling.h \
 io/pdqio.h \
 index/mih.h \
 hashing/torben.h \
 ./dll/dll_interface.h \
 ./CImg.h \
 ../../utils/getline.h

LIBSRCS=\
 common/pdqhashtypes.cpp \
 io/hashio.cpp \
 common/pdqhamming.cpp \
 hashing/pdqhashing.cpp \
 downscaling/downscaling.cpp \
 io/pdqio.cpp \
 hashing/torben.cpp \
 ./dll/dll_interface.cpp

LIBOBJS=\
 ./common/pdqhashtypes.o \
 ./io/hashio.o \
 ./common/pdqhamming.o \
 ./hashing/pdqhashing.o \
 ./downscaling/downscaling.o \
 ./io/pdqio.o \
 ./hashing/torben.o \
 ./dll/dll_interface.o \
 ../../utils/getline.o

MAINS=\
 pdq-photo-hasher-new.exe
#  test-mih \
#  clusterize256 \
#  snowball-clusterize256 \
#  clusterize256x \
#  mih-query \
#  hashtool256 \
#  pdq-downsample-demo \
#  test-bits \
#  hamming-tabulate \
#  test-hamming

TESTS=\
 run-test-bits run-test-hamming run-reg-test

# ================================================================
top: $(MAINS) # $(TESTS)

run-test-bits: test-bits
	./test-bits
run-test-hamming: test-hamming
	./test-hamming
run-reg-test:
	./reg_test/run
itso:
	cp output-regtest/out reg_test/expected

libpdq.a: $(LIBOBJS)
	$(AR) r $@ $(LIBOBJS)

libpdq.dll: $(LIBOBJS)
	$(CXX) -shared -o $@ $(LIBOBJS) $(LFLAGS) $(CIMG) -Wl,--out-implib,libpdqdll.a

# ================================================================
./common/pdqhashtypes.o: ./common/pdqhashtypes.cpp $(LIBHDRS)
	$(CCOPT) -c ./common/pdqhashtypes.cpp -o ./common/pdqhashtypes.o

./io/hashio.o: ./io/hashio.cpp $(LIBHDRS)
	$(CCOPT) -c ./io/hashio.cpp -o ./io/hashio.o

./common/pdqhamming.o: ./common/pdqhamming.cpp $(LIBHDRS)
	$(CCOPT) -c ./common/pdqhamming.cpp -o ./common/pdqhamming.o

./hashing/pdqhashing.o: ./hashing/pdqhashing.cpp $(LIBHDRS)
	$(CCOPT) -c ./hashing/pdqhashing.cpp -o ./hashing/pdqhashing.o

./downscaling/downscaling.o: ./downscaling/downscaling.cpp $(LIBHDRS)
	$(CCOPT) -c ./downscaling/downscaling.cpp -o ./downscaling/downscaling.o

./io/pdqio.o: ./io/pdqio.cpp $(LIBHDRS)
	$(CCOPT) -c ./io/pdqio.cpp -o ./io/pdqio.o $(LFLAGS) $(CIMG)

./hashing/torben.o: ./hashing/torben.cpp $(LIBHDRS)
	$(CCOPT) -c ./hashing/torben.cpp -o ./hashing/torben.o

./dll/dll_interface.o: ./dll/dll_interface.cpp $(LIBHDRS)
	$(CCOPT) -DPDQ_EXPORTS -c ./dll/dll_interface.cpp -o ./dll/dll_interface.o

../../utils/getline.o: ../../utils/getline.c ../../utils/getline.h
	$(COPT) -c ../../utils/getline.c -o ../../utils/getline.o

# ================================================================
hashtool256: bin/hashtool256.cpp $(LIBHDRS) $(LIBOBJS) libpdq.a
	$(CCOPT) bin/hashtool256.cpp -o hashtool256 $(LFLAGS)

test-bits: bin/test-bits.cpp $(LIBHDRS) $(LIBOBJS) libpdq.a
	$(CCOPT) bin/test-bits.cpp -o test-bits $(LFLAGS)

hamming-tabulate: bin/hamming-tabulate.cpp $(LIBHDRS) $(LIBOBJS) libpdq.a
	$(CCOPT) bin/hamming-tabulate.cpp -o test-hamming $(LFLAGS)

test-hamming: bin/test-hamming.cpp $(LIBHDRS) $(LIBOBJS) libpdq.a
	$(CCOPT) bin/test-hamming.cpp -o test-hamming $(LFLAGS)

pdq-photo-hasher-new.exe: bin/pdq-photo-hasher.cpp $(LIBHDRS) libpdq.a
	$(CCOPT) bin/pdq-photo-hasher.cpp -o $@ -L. -lpdq $(LFLAGS) $(CIMG)

pdq-photo-hasher.exe: bin/pdq-photo-hasher.cpp $(LIBHDRS) $(LIBSRCS) ../../utils/getline.o
	$(CCOPT) bin/pdq-photo-hasher.cpp $(LIBSRCS) ../../utils/getline.o -o $@ $(LFLAGS) $(CIMG)

# Profile version. Usage:
# * make pdq-photo-hasher-prof
# * pdq-photo-hasher-prof {arguments>
# * gprof pdq-photo-hasher-prof gmon.out > myfile.txt
# Note: this profiler command works on Linux; not on OSX.
pdq-photo-hasher-prof: bin/pdq-photo-hasher.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCDBG) -pg bin/pdq-photo-hasher.cpp $(LIBSRCS) -o pdq-photo-hasher-prof $(LFLAGS) -lpthread

pdqtestg: bin/pdq-photo-hasher.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCDBG) bin/pdq-photo-hasher.cpp $(LIBSRCS) -o pdqtestg $(LFLAGS)
pdqtestsan: bin/pdq-photo-hasher.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCSAN) bin/pdq-photo-hasher.cpp $(LIBSRCS) -o pdqtestsan $(LFLAGS)

pdq-downsample-demo: bin/pdq-downsample-demo.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCOPT) bin/pdq-downsample-demo.cpp $(LIBSRCS) -o pdq-downsample-demo $(LFLAGS)

test-mih: bin/test-mih.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCOPT) bin/test-mih.cpp $(LIBSRCS) -o test-mih $(LFLAGS)
test-mihg: bin/test-mih.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCDBG) bin/test-mih.cpp $(LIBSRCS) -o test-mihg $(LFLAGS)
clusterize256: bin/clusterize256.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCOPT) bin/clusterize256.cpp $(LIBSRCS) -o clusterize256 $(LFLAGS)
clusterize256-prof: bin/clusterize256.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCDBG) -pg bin/clusterize256.cpp $(LIBSRCS) -o clusterize256-prof $(LFLAGS) -lpthread
snowball-clusterize256: bin/snowball-clusterize256.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCOPT) bin/snowball-clusterize256.cpp $(LIBSRCS) -o snowball-clusterize256 $(LFLAGS)
clusterize256x: bin/clusterize256x.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCOPT) bin/clusterize256x.cpp $(LIBSRCS) -o clusterize256x $(LFLAGS)
clusterize256x-prof: bin/clusterize256x.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCDBG) -pg bin/clusterize256x.cpp $(LIBSRCS) -o clusterize256x-prof $(LFLAGS) -lpthread
mih-query: bin/mih-query.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCOPT) bin/mih-query.cpp $(LIBSRCS) -o mih-query $(LFLAGS)

# Profile version. Usage:
# * make pdq-photo-hasher-prof
# * pdq-photo-hasher-prof {arguments>
# * gprof pdq-photo-hasher-prof gmon.out > myfile.txt
# Note: works on Linux; not on OSX.
test-mih-prof: bin/test-mih.cpp $(LIBSRCS) $(LIBHDRS)
	$(CCDBG) -pg bin/test-mih.cpp $(LIBSRCS) -o test-mih-prof $(LFLAGS) -lpthread

# ================================================================
tags: .always
	ctags -R .
clean:
	@-rm -vf $(MAINS) tags
	@-rm -rvf *.dSYM
	@-rm -vf libpdq.a libpdq.dll libpdqdll.a
	@-rm -vf $(LIBOBJS)
.always:
	/usr/bin/true
