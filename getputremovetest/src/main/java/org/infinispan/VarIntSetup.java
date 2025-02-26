package org.infinispan;

import java.io.DataOutput;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class VarIntSetup {
   byte[] bytes;

   @Setup
   public void setup() {
      bytes = new byte[5];
   }

   byte[] getBytes() {
      return bytes;
   }
}
