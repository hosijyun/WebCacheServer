package org.weefic.cache.web

import java.io.IOException

class WebException(val code: Int, cause: Exception? = null) : IOException(cause)