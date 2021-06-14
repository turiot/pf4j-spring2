package org.pf4j.spring2;

public class UpdateException extends RuntimeException {

   private final String pluginId;

   public UpdateException(String pluginId, String message) {
      super(message);
      this.pluginId = pluginId;
   }

   public String getPluginId() {
      return pluginId;
   }

}
