package online.twelvesteps.anarucombot;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

final class BinaryResourceContentSupplier implements Supplier<byte[]> {
  private final String name;
  private final BiFunction<IOException, String, byte[]> ifCantLoad;

  public BinaryResourceContentSupplier(
      String name, BiFunction<IOException, String, byte[]> ifCantLoad) {
    this.name = checkNotNull(name);
    this.ifCantLoad = checkNotNull(ifCantLoad);
  }

  @Override
  public byte[] get() {
    try (InputStream is = getClass().getResourceAsStream(name)) {
      if (is == null) {
        return ifCantLoad.apply(null, name);
      }
      return is.readAllBytes();
    } catch (IOException ex) {
      return ifCantLoad.apply(ex, name);
    }
  }
}

