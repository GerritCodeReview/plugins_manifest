package com.googlesource.gerrit.plugins.manifest;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public abstract class TwoArgumentOptionHandler<T> extends OptionHandler<T> {

  public TwoArgumentOptionHandler(CmdLineParser parser, OptionDef option,
      Setter<? super T> setter) {
    super(parser, option, setter);
  }

  @Override
  public String getDefaultMetaVariable() {
    return "ARG ARG";
  }

  @Override
  public int parseArguments(Parameters params) throws CmdLineException {
    if (params.size() < 2) {
      throw new IllegalArgumentException("Expecting 2 arguments");
    }

    String firstArgument = params.getParameter(0);
    String secondArgument = params.getParameter(1);

    T value = parse(firstArgument, secondArgument);
    setter.addValue(value);
    return 2;
  }

  /**
   * Parses a string to a real value of Type &lt;T&gt;.
   * @param arg1 First string value to parse
   * @param arg2 Second string value to parse
   * @return the parsed value
   * @throws CmdLineException
   *      if the parsing encounters a failure that should be reported to the user.
   */
  protected abstract T parse(String arg1, String arg2)
      throws CmdLineException;
}
