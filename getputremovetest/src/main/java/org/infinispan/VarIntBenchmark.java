package org.infinispan;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class VarIntBenchmark {

   public static final VarHandle INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);

   @State(Scope.Thread) // Added State class
   public static class ByteArrayState {
      byte[] byteArray = new byte[5];

      @Param({"1", "127", "128", "16383", "16384", "2097151", "2097152", "268435455", "268435456", "2147483647"})
      int value;
   }

   @Benchmark
   public void suggestedWrite(Blackhole blackhole, ByteArrayState state) {
      int val = state.value;
      int index = 0;
      while ((val & ~0x7F) != 0) {
         state.byteArray[index++] = (byte) ((val & 0x7F) | 0x80);
         val >>>= 7;
      }
      state.byteArray[index] = (byte) val;
      blackhole.consume(state.byteArray);
   }

   @Benchmark
   public void TagWriter(Blackhole blackhole, ByteArrayState state) {
      int val = state.value;
      int pos = 0;
      while (true) {
         if ((val & 0xFFFFFF80) == 0) {
            state.byteArray[pos] = (byte) val;
            break;
         } else {
            state.byteArray[pos++] = (byte) (val & 0x7F | 0x80);
            val >>>= 7;
         }
      }
      blackhole.consume(state.byteArray);
   }

   @Benchmark
   public void ifWrite(Blackhole blackhole, ByteArrayState state) {
      int val = state.value;
      if ((val & -128) == 0) {
         state.byteArray[0] = (byte) val;
      } else if ((val & -16384) == 0) {
         state.byteArray[0] = (byte) (val & 0x7F | 0x80);
         state.byteArray[1] = (byte) (val >>> 7 & 0x7F);
      } else if ((val & -2097152) == 0) {
         state.byteArray[0] = (byte) (val & 0x7F | 0x80);
         state.byteArray[1] = (byte) (val >>> 7 & 0x7F | 0x80);
         state.byteArray[2] = (byte) (val >>> 14 & 0x7F);
      } else if ((val & -268435456) == 0) {
         state.byteArray[0] = (byte) (val & 0x7F | 0x80);
         state.byteArray[1] = (byte) (val >>> 7 & 0x7F | 0x80);
         state.byteArray[2] = (byte) (val >>> 14 & 0x7F | 0x80);
         state.byteArray[3] = (byte) (val >>> 21 & 0x7F);
      } else {
         state.byteArray[0] = (byte) (val & 0x7F | 0x80);
         state.byteArray[1] = (byte) (val >>> 7 & 0x7F | 0x80);
         state.byteArray[2] = (byte) (val >>> 14 & 0x7F | 0x80);
         state.byteArray[3] = (byte) (val >>> 21 & 0x7F | 0x80);
         state.byteArray[4] = (byte) (val >>> 28 & 0x7F);
      }
      blackhole.consume(state.byteArray);
   }
}
