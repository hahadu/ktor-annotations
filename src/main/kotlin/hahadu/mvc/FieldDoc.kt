package hahadu.mvc

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldDoc(
    val description: String = "",
    val example: String = "",
    val enum: Array<String> = [],
)
