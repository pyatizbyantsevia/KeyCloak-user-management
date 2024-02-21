import groovy.transform.Field

/**
 * Wrap-class, представляющий обертку для enum.
 */
public class LogLevel {

  /**
     * Enum уровней логирования.
  */
  public enum Values {
    INFO,
    ERROR,
    WARNING
  }

  public final LogLevel.Values INFO = LogLevel.Values.INFO
  public final LogLevel.Values ERROR = LogLevel.Values.ERROR
  public final LogLevel.Values WARNING = LogLevel.Values.WARNING
}

// Экземпляр класса LogLevel для единого обращения к элементам enum
@Field final LogLevel levels = new LogLevel()

return this
