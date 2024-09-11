package utils

object FileUtils {
  val MaxFileSize: Long = 5 * 1024 * 1024
  val AllowedExtensions: Set[String] = Set("jpg", "jpeg", "png")

  def isValidExtension(filename: String): Boolean = {
    val extension = filename.split("\\.").lastOption.getOrElse("").toLowerCase
    AllowedExtensions.contains(extension)
  }

  def isValidSize(fileSize: Long): Boolean = {
    fileSize <= MaxFileSize
  }
}
