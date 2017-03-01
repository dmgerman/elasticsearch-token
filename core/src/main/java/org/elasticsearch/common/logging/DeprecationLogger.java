begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.logging
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|SuppressLoggerChecks
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZoneId
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZonedDateTime
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatterBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|SignStyle
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArraySet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|DAY_OF_MONTH
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|DAY_OF_WEEK
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|HOUR_OF_DAY
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|MINUTE_OF_HOUR
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|MONTH_OF_YEAR
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|SECOND_OF_MINUTE
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|YEAR
import|;
end_import

begin_comment
comment|/**  * A logger that logs deprecation notices.  */
end_comment

begin_class
DECL|class|DeprecationLogger
specifier|public
class|class
name|DeprecationLogger
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
comment|/**      * This is set once by the {@code Node} constructor, but it uses {@link CopyOnWriteArraySet} to ensure that tests can run in parallel.      *<p>      * Integration tests will create separate nodes within the same classloader, thus leading to a shared, {@code static} state.      * In order for all tests to appropriately be handled, this must be able to remember<em>all</em> {@link ThreadContext}s that it is      * given in a thread safe manner.      *<p>      * For actual usage, multiple nodes do not share the same JVM and therefore this will only be set once in practice.      */
DECL|field|THREAD_CONTEXT
specifier|private
specifier|static
specifier|final
name|CopyOnWriteArraySet
argument_list|<
name|ThreadContext
argument_list|>
name|THREAD_CONTEXT
init|=
operator|new
name|CopyOnWriteArraySet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Set the {@link ThreadContext} used to add deprecation headers to network responses.      *<p>      * This is expected to<em>only</em> be invoked by the {@code Node}'s constructor (therefore once outside of tests).      *      * @param threadContext The thread context owned by the {@code ThreadPool} (and implicitly a {@code Node})      * @throws IllegalStateException if this {@code threadContext} has already been set      */
DECL|method|setThreadContext
specifier|public
specifier|static
name|void
name|setThreadContext
parameter_list|(
name|ThreadContext
name|threadContext
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|threadContext
argument_list|,
literal|"Cannot register a null ThreadContext"
argument_list|)
expr_stmt|;
comment|// add returning false means it _did_ have it already
if|if
condition|(
name|THREAD_CONTEXT
operator|.
name|add
argument_list|(
name|threadContext
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Double-setting ThreadContext not allowed!"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Remove the {@link ThreadContext} used to add deprecation headers to network responses.      *<p>      * This is expected to<em>only</em> be invoked by the {@code Node}'s {@code close} method (therefore once outside of tests).      *      * @param threadContext The thread context owned by the {@code ThreadPool} (and implicitly a {@code Node})      * @throws IllegalStateException if this {@code threadContext} is unknown (and presumably already unset before)      */
DECL|method|removeThreadContext
specifier|public
specifier|static
name|void
name|removeThreadContext
parameter_list|(
name|ThreadContext
name|threadContext
parameter_list|)
block|{
assert|assert
name|threadContext
operator|!=
literal|null
assert|;
comment|// remove returning false means it did not have it already
if|if
condition|(
name|THREAD_CONTEXT
operator|.
name|remove
argument_list|(
name|threadContext
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Removing unknown ThreadContext not allowed!"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Creates a new deprecation logger based on the parent logger. Automatically      * prefixes the logger name with "deprecation", if it starts with "org.elasticsearch.",      * it replaces "org.elasticsearch" with "org.elasticsearch.deprecation" to maintain      * the "org.elasticsearch" namespace.      */
DECL|method|DeprecationLogger
specifier|public
name|DeprecationLogger
parameter_list|(
name|Logger
name|parentLogger
parameter_list|)
block|{
name|String
name|name
init|=
name|parentLogger
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"org.elasticsearch"
argument_list|)
condition|)
block|{
name|name
operator|=
name|name
operator|.
name|replace
argument_list|(
literal|"org.elasticsearch."
argument_list|,
literal|"org.elasticsearch.deprecation."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|name
operator|=
literal|"deprecation."
operator|+
name|name
expr_stmt|;
block|}
name|this
operator|.
name|logger
operator|=
name|LogManager
operator|.
name|getLogger
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**      * Logs a deprecated message.      */
DECL|method|deprecated
specifier|public
name|void
name|deprecated
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|params
parameter_list|)
block|{
name|deprecated
argument_list|(
name|THREAD_CONTEXT
argument_list|,
name|msg
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
comment|/*      * RFC7234 specifies the warning format as warn-code<space> warn-agent<space> "warn-text" [<space> "warn-date"]. Here, warn-code is a      * three-digit number with various standard warn codes specified. The warn code 299 is apt for our purposes as it represents a      * miscellaneous persistent warning (can be presented to a human, or logged, and must not be removed by a cache). The warn-agent is an      * arbitrary token; here we use the Elasticsearch version and build hash. The warn text must be quoted. The warn-date is an optional      * quoted field that can be in a variety of specified date formats; here we use RFC 1123 format.      */
DECL|field|WARNING_FORMAT
specifier|private
specifier|static
specifier|final
name|String
name|WARNING_FORMAT
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"299 Elasticsearch-%s%s-%s "
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|toString
argument_list|()
argument_list|,
name|Build
operator|.
name|CURRENT
operator|.
name|isSnapshot
argument_list|()
condition|?
literal|"-SNAPSHOT"
else|:
literal|""
argument_list|,
name|Build
operator|.
name|CURRENT
operator|.
name|shortHash
argument_list|()
argument_list|)
operator|+
literal|"\"%s\" \"%s\""
decl_stmt|;
comment|/*      * RFC 7234 section 5.5 specifies that the warn-date is a quoted HTTP-date. HTTP-date is defined in RFC 7234 Appendix B as being from      * RFC 7231 section 7.1.1.1. RFC 7231 specifies an HTTP-date as an IMF-fixdate (or an obs-date referring to obsolete formats). The      * grammar for IMF-fixdate is specified as 'day-name "," SP date1 SP time-of-day SP GMT'. Here, day-name is      * (Mon|Tue|Wed|Thu|Fri|Sat|Sun). Then, date1 is 'day SP month SP year' where day is 2DIGIT, month is      * (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec), and year is 4DIGIT. Lastly, time-of-day is 'hour ":" minute ":" second' where      * hour is 2DIGIT, minute is 2DIGIT, and second is 2DIGIT. Finally, 2DIGIT and 4DIGIT have the obvious definitions.      */
DECL|field|RFC_7231_DATE_TIME
specifier|private
specifier|static
specifier|final
name|DateTimeFormatter
name|RFC_7231_DATE_TIME
decl_stmt|;
static|static
block|{
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|String
argument_list|>
name|dow
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|1L
argument_list|,
literal|"Mon"
argument_list|)
expr_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|2L
argument_list|,
literal|"Tue"
argument_list|)
expr_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|3L
argument_list|,
literal|"Wed"
argument_list|)
expr_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|4L
argument_list|,
literal|"Thu"
argument_list|)
expr_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|5L
argument_list|,
literal|"Fri"
argument_list|)
expr_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|6L
argument_list|,
literal|"Sat"
argument_list|)
expr_stmt|;
name|dow
operator|.
name|put
argument_list|(
literal|7L
argument_list|,
literal|"Sun"
argument_list|)
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|String
argument_list|>
name|moy
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|1L
argument_list|,
literal|"Jan"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|2L
argument_list|,
literal|"Feb"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|3L
argument_list|,
literal|"Mar"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|4L
argument_list|,
literal|"Apr"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|5L
argument_list|,
literal|"May"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|6L
argument_list|,
literal|"Jun"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|7L
argument_list|,
literal|"Jul"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|8L
argument_list|,
literal|"Aug"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|9L
argument_list|,
literal|"Sep"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|10L
argument_list|,
literal|"Oct"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|11L
argument_list|,
literal|"Nov"
argument_list|)
expr_stmt|;
name|moy
operator|.
name|put
argument_list|(
literal|12L
argument_list|,
literal|"Dec"
argument_list|)
expr_stmt|;
name|RFC_7231_DATE_TIME
operator|=
operator|new
name|DateTimeFormatterBuilder
argument_list|()
operator|.
name|parseCaseInsensitive
argument_list|()
operator|.
name|parseLenient
argument_list|()
operator|.
name|optionalStart
argument_list|()
operator|.
name|appendText
argument_list|(
name|DAY_OF_WEEK
argument_list|,
name|dow
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|", "
argument_list|)
operator|.
name|optionalEnd
argument_list|()
operator|.
name|appendValue
argument_list|(
name|DAY_OF_MONTH
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
name|SignStyle
operator|.
name|NOT_NEGATIVE
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|' '
argument_list|)
operator|.
name|appendText
argument_list|(
name|MONTH_OF_YEAR
argument_list|,
name|moy
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|' '
argument_list|)
operator|.
name|appendValue
argument_list|(
name|YEAR
argument_list|,
literal|4
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|' '
argument_list|)
operator|.
name|appendValue
argument_list|(
name|HOUR_OF_DAY
argument_list|,
literal|2
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|':'
argument_list|)
operator|.
name|appendValue
argument_list|(
name|MINUTE_OF_HOUR
argument_list|,
literal|2
argument_list|)
operator|.
name|optionalStart
argument_list|()
operator|.
name|appendLiteral
argument_list|(
literal|':'
argument_list|)
operator|.
name|appendValue
argument_list|(
name|SECOND_OF_MINUTE
argument_list|,
literal|2
argument_list|)
operator|.
name|optionalEnd
argument_list|()
operator|.
name|appendLiteral
argument_list|(
literal|' '
argument_list|)
operator|.
name|appendOffset
argument_list|(
literal|"+HHMM"
argument_list|,
literal|"GMT"
argument_list|)
operator|.
name|toFormatter
argument_list|(
name|Locale
operator|.
name|getDefault
argument_list|(
name|Locale
operator|.
name|Category
operator|.
name|FORMAT
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|field|GMT
specifier|private
specifier|static
specifier|final
name|ZoneId
name|GMT
init|=
name|ZoneId
operator|.
name|of
argument_list|(
literal|"GMT"
argument_list|)
decl_stmt|;
comment|/**      * Regular expression to test if a string matches the RFC7234 specification for warning headers. This pattern assumes that the warn code      * is always 299. Further, this pattern assumes that the warn agent represents a version of Elasticsearch including the build hash.      */
DECL|field|WARNING_HEADER_PATTERN
specifier|public
specifier|static
name|Pattern
name|WARNING_HEADER_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"299 "
operator|+
comment|// warn code
literal|"Elasticsearch-\\d+\\.\\d+\\.\\d+(?:-(?:alpha|beta|rc)\\d+)?(?:-SNAPSHOT)?-(?:[a-f0-9]{7}|Unknown) "
operator|+
comment|// warn agent
literal|"\"((?:\t| |!|[\\x23-\\x5b]|[\\x5d-\\x7e]|[\\x80-\\xff]|\\\\|\\\\\")*)\" "
operator|+
comment|// quoted warning value, captured
comment|// quoted RFC 1123 date format
literal|"\""
operator|+
comment|// opening quote
literal|"(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun), "
operator|+
comment|// weekday
literal|"\\d{2} "
operator|+
comment|// 2-digit day
literal|"(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) "
operator|+
comment|// month
literal|"\\d{4} "
operator|+
comment|// 4-digit year
literal|"\\d{2}:\\d{2}:\\d{2} "
operator|+
comment|// (two-digit hour):(two-digit minute):(two-digit second)
literal|"GMT"
operator|+
comment|// GMT
literal|"\""
argument_list|)
decl_stmt|;
comment|// closing quote
comment|/**      * Extracts the warning value from the value of a warning header that is formatted according to RFC 7234. That is, given a string      * {@code 299 Elasticsearch-6.0.0 "warning value" "Sat, 25 Feb 2017 10:27:43 GMT"}, the return value of this method would be {@code      * warning value}.      *      * @param s the value of a warning header formatted according to RFC 7234.      * @return the extracted warning value      */
DECL|method|extractWarningValueFromWarningHeader
specifier|public
specifier|static
name|String
name|extractWarningValueFromWarningHeader
parameter_list|(
specifier|final
name|String
name|s
parameter_list|)
block|{
specifier|final
name|Matcher
name|matcher
init|=
name|WARNING_HEADER_PATTERN
operator|.
name|matcher
argument_list|(
name|s
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|matches
init|=
name|matcher
operator|.
name|matches
argument_list|()
decl_stmt|;
assert|assert
name|matches
assert|;
return|return
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
return|;
block|}
comment|/**      * Logs a deprecated message to the deprecation log, as well as to the local {@link ThreadContext}.      *      * @param threadContexts The node's {@link ThreadContext} (outside of concurrent tests, this should only ever have one context).      * @param message The deprecation message.      * @param params The parameters used to fill in the message, if any exist.      */
annotation|@
name|SuppressLoggerChecks
argument_list|(
name|reason
operator|=
literal|"safely delegates to logger"
argument_list|)
DECL|method|deprecated
name|void
name|deprecated
parameter_list|(
specifier|final
name|Set
argument_list|<
name|ThreadContext
argument_list|>
name|threadContexts
parameter_list|,
specifier|final
name|String
name|message
parameter_list|,
specifier|final
name|Object
modifier|...
name|params
parameter_list|)
block|{
specifier|final
name|Iterator
argument_list|<
name|ThreadContext
argument_list|>
name|iterator
init|=
name|threadContexts
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
specifier|final
name|String
name|formattedMessage
init|=
name|LoggerMessageFormat
operator|.
name|format
argument_list|(
name|message
argument_list|,
name|params
argument_list|)
decl_stmt|;
specifier|final
name|String
name|warningHeaderValue
init|=
name|formatWarning
argument_list|(
name|formattedMessage
argument_list|)
decl_stmt|;
assert|assert
name|WARNING_HEADER_PATTERN
operator|.
name|matcher
argument_list|(
name|warningHeaderValue
argument_list|)
operator|.
name|matches
argument_list|()
assert|;
assert|assert
name|extractWarningValueFromWarningHeader
argument_list|(
name|warningHeaderValue
argument_list|)
operator|.
name|equals
argument_list|(
name|escape
argument_list|(
name|formattedMessage
argument_list|)
argument_list|)
assert|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
try|try
block|{
specifier|final
name|ThreadContext
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|next
operator|.
name|addResponseHeader
argument_list|(
literal|"Warning"
argument_list|,
name|warningHeaderValue
argument_list|,
name|DeprecationLogger
operator|::
name|extractWarningValueFromWarningHeader
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// ignored; it should be removed shortly
block|}
block|}
name|logger
operator|.
name|warn
argument_list|(
name|formattedMessage
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
name|message
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Format a warning string in the proper warning format by prepending a warn code, warn agent, wrapping the warning string in quotes,      * and appending the RFC 7231 date.      *      * @param s the warning string to format      * @return a warning value formatted according to RFC 7234      */
DECL|method|formatWarning
specifier|public
specifier|static
name|String
name|formatWarning
parameter_list|(
specifier|final
name|String
name|s
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
name|WARNING_FORMAT
argument_list|,
name|escape
argument_list|(
name|s
argument_list|)
argument_list|,
name|RFC_7231_DATE_TIME
operator|.
name|format
argument_list|(
name|ZonedDateTime
operator|.
name|now
argument_list|(
name|GMT
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Escape backslashes and quotes in the specified string.      *      * @param s the string to escape      * @return the escaped string      */
DECL|method|escape
specifier|public
specifier|static
name|String
name|escape
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
name|s
operator|.
name|replaceAll
argument_list|(
literal|"(\\\\|\")"
argument_list|,
literal|"\\\\$1"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

