package military.engine.ai;

/**
 * Marker interface for AI-planned actions. Concrete implementations are immutable
 * value objects that describe domain operations for the engine to execute.
 *
 * Java 8 does not support sealed types; use instanceof checks or a visitor when needed.
 */
public interface AiAction {
}
