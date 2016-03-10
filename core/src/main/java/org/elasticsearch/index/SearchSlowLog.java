begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|settings
operator|.
name|Setting
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SearchSlowLog
specifier|public
specifier|final
class|class
name|SearchSlowLog
block|{
DECL|field|reformat
specifier|private
name|boolean
name|reformat
decl_stmt|;
DECL|field|queryWarnThreshold
specifier|private
name|long
name|queryWarnThreshold
decl_stmt|;
DECL|field|queryInfoThreshold
specifier|private
name|long
name|queryInfoThreshold
decl_stmt|;
DECL|field|queryDebugThreshold
specifier|private
name|long
name|queryDebugThreshold
decl_stmt|;
DECL|field|queryTraceThreshold
specifier|private
name|long
name|queryTraceThreshold
decl_stmt|;
DECL|field|fetchWarnThreshold
specifier|private
name|long
name|fetchWarnThreshold
decl_stmt|;
DECL|field|fetchInfoThreshold
specifier|private
name|long
name|fetchInfoThreshold
decl_stmt|;
DECL|field|fetchDebugThreshold
specifier|private
name|long
name|fetchDebugThreshold
decl_stmt|;
DECL|field|fetchTraceThreshold
specifier|private
name|long
name|fetchTraceThreshold
decl_stmt|;
DECL|field|level
specifier|private
name|SlowLogLevel
name|level
decl_stmt|;
DECL|field|queryLogger
specifier|private
specifier|final
name|ESLogger
name|queryLogger
decl_stmt|;
DECL|field|fetchLogger
specifier|private
specifier|final
name|ESLogger
name|fetchLogger
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_PREFIX
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_PREFIX
init|=
literal|"index.search.slowlog"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.warn"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.info"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.debug"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.trace"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.warn"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.info"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.debug"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.trace"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_REFORMAT
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|INDEX_SEARCH_SLOWLOG_REFORMAT
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".reformat"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_LEVEL
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|SlowLogLevel
argument_list|>
name|INDEX_SEARCH_SLOWLOG_LEVEL
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".level"
argument_list|,
name|SlowLogLevel
operator|.
name|TRACE
operator|.
name|name
argument_list|()
argument_list|,
name|SlowLogLevel
operator|::
name|parse
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|method|SearchSlowLog
specifier|public
name|SearchSlowLog
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|this
operator|.
name|queryLogger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".query"
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchLogger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".fetch"
argument_list|)
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_REFORMAT
argument_list|,
name|this
operator|::
name|setReformat
argument_list|)
expr_stmt|;
name|this
operator|.
name|reformat
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_REFORMAT
argument_list|)
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN_SETTING
argument_list|,
name|this
operator|::
name|setQueryWarnThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryWarnThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO_SETTING
argument_list|,
name|this
operator|::
name|setQueryInfoThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryInfoThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG_SETTING
argument_list|,
name|this
operator|::
name|setQueryDebugThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryDebugThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE_SETTING
argument_list|,
name|this
operator|::
name|setQueryTraceThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryTraceThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN_SETTING
argument_list|,
name|this
operator|::
name|setFetchWarnThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchWarnThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO_SETTING
argument_list|,
name|this
operator|::
name|setFetchInfoThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchInfoThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG_SETTING
argument_list|,
name|this
operator|::
name|setFetchDebugThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchDebugThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE_SETTING
argument_list|,
name|this
operator|::
name|setFetchTraceThreshold
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchTraceThreshold
operator|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE_SETTING
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|getScopedSettings
argument_list|()
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_SEARCH_SLOWLOG_LEVEL
argument_list|,
name|this
operator|::
name|setLevel
argument_list|)
expr_stmt|;
name|setLevel
argument_list|(
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_SEARCH_SLOWLOG_LEVEL
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|setLevel
specifier|private
name|void
name|setLevel
parameter_list|(
name|SlowLogLevel
name|level
parameter_list|)
block|{
name|this
operator|.
name|level
operator|=
name|level
expr_stmt|;
name|this
operator|.
name|queryLogger
operator|.
name|setLevel
argument_list|(
name|level
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchLogger
operator|.
name|setLevel
argument_list|(
name|level
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|onQueryPhase
specifier|public
name|void
name|onQueryPhase
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|long
name|tookInNanos
parameter_list|)
block|{
if|if
condition|(
name|queryWarnThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|queryWarnThreshold
condition|)
block|{
name|queryLogger
operator|.
name|warn
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|queryInfoThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|queryInfoThreshold
condition|)
block|{
name|queryLogger
operator|.
name|info
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|queryDebugThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|queryDebugThreshold
condition|)
block|{
name|queryLogger
operator|.
name|debug
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|queryTraceThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|queryTraceThreshold
condition|)
block|{
name|queryLogger
operator|.
name|trace
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onFetchPhase
specifier|public
name|void
name|onFetchPhase
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|long
name|tookInNanos
parameter_list|)
block|{
if|if
condition|(
name|fetchWarnThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|fetchWarnThreshold
condition|)
block|{
name|fetchLogger
operator|.
name|warn
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fetchInfoThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|fetchInfoThreshold
condition|)
block|{
name|fetchLogger
operator|.
name|info
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fetchDebugThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|fetchDebugThreshold
condition|)
block|{
name|fetchLogger
operator|.
name|debug
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fetchTraceThreshold
operator|>=
literal|0
operator|&&
name|tookInNanos
operator|>
name|fetchTraceThreshold
condition|)
block|{
name|fetchLogger
operator|.
name|trace
argument_list|(
literal|"{}"
argument_list|,
operator|new
name|SlowLogSearchContextPrinter
argument_list|(
name|context
argument_list|,
name|tookInNanos
argument_list|,
name|reformat
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SlowLogSearchContextPrinter
specifier|private
specifier|static
class|class
name|SlowLogSearchContextPrinter
block|{
DECL|field|context
specifier|private
specifier|final
name|SearchContext
name|context
decl_stmt|;
DECL|field|tookInNanos
specifier|private
specifier|final
name|long
name|tookInNanos
decl_stmt|;
DECL|field|reformat
specifier|private
specifier|final
name|boolean
name|reformat
decl_stmt|;
DECL|method|SlowLogSearchContextPrinter
specifier|public
name|SlowLogSearchContextPrinter
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|long
name|tookInNanos
parameter_list|,
name|boolean
name|reformat
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|tookInNanos
operator|=
name|tookInNanos
expr_stmt|;
name|this
operator|.
name|reformat
operator|=
name|reformat
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"took["
argument_list|)
operator|.
name|append
argument_list|(
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|tookInNanos
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], took_millis["
argument_list|)
operator|.
name|append
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|tookInNanos
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|getTypes
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"types[], "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"types["
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|arrayToDelimitedString
argument_list|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|getTypes
argument_list|()
argument_list|,
literal|","
argument_list|,
name|sb
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|context
operator|.
name|groupStats
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"stats[], "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"stats["
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|collectionToDelimitedString
argument_list|(
name|context
operator|.
name|groupStats
argument_list|()
argument_list|,
literal|","
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
name|sb
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"search_type["
argument_list|)
operator|.
name|append
argument_list|(
name|context
operator|.
name|searchType
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], total_shards["
argument_list|)
operator|.
name|append
argument_list|(
name|context
operator|.
name|numberOfShards
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|request
argument_list|()
operator|.
name|source
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"source["
argument_list|)
operator|.
name|append
argument_list|(
name|context
operator|.
name|request
argument_list|()
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"source[], "
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
DECL|method|setReformat
specifier|private
name|void
name|setReformat
parameter_list|(
name|boolean
name|reformat
parameter_list|)
block|{
name|this
operator|.
name|reformat
operator|=
name|reformat
expr_stmt|;
block|}
DECL|method|setQueryWarnThreshold
specifier|private
name|void
name|setQueryWarnThreshold
parameter_list|(
name|TimeValue
name|warnThreshold
parameter_list|)
block|{
name|this
operator|.
name|queryWarnThreshold
operator|=
name|warnThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setQueryInfoThreshold
specifier|private
name|void
name|setQueryInfoThreshold
parameter_list|(
name|TimeValue
name|infoThreshold
parameter_list|)
block|{
name|this
operator|.
name|queryInfoThreshold
operator|=
name|infoThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setQueryDebugThreshold
specifier|private
name|void
name|setQueryDebugThreshold
parameter_list|(
name|TimeValue
name|debugThreshold
parameter_list|)
block|{
name|this
operator|.
name|queryDebugThreshold
operator|=
name|debugThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setQueryTraceThreshold
specifier|private
name|void
name|setQueryTraceThreshold
parameter_list|(
name|TimeValue
name|traceThreshold
parameter_list|)
block|{
name|this
operator|.
name|queryTraceThreshold
operator|=
name|traceThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setFetchWarnThreshold
specifier|private
name|void
name|setFetchWarnThreshold
parameter_list|(
name|TimeValue
name|warnThreshold
parameter_list|)
block|{
name|this
operator|.
name|fetchWarnThreshold
operator|=
name|warnThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setFetchInfoThreshold
specifier|private
name|void
name|setFetchInfoThreshold
parameter_list|(
name|TimeValue
name|infoThreshold
parameter_list|)
block|{
name|this
operator|.
name|fetchInfoThreshold
operator|=
name|infoThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setFetchDebugThreshold
specifier|private
name|void
name|setFetchDebugThreshold
parameter_list|(
name|TimeValue
name|debugThreshold
parameter_list|)
block|{
name|this
operator|.
name|fetchDebugThreshold
operator|=
name|debugThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|setFetchTraceThreshold
specifier|private
name|void
name|setFetchTraceThreshold
parameter_list|(
name|TimeValue
name|traceThreshold
parameter_list|)
block|{
name|this
operator|.
name|fetchTraceThreshold
operator|=
name|traceThreshold
operator|.
name|nanos
argument_list|()
expr_stmt|;
block|}
DECL|method|isReformat
name|boolean
name|isReformat
parameter_list|()
block|{
return|return
name|reformat
return|;
block|}
DECL|method|getQueryWarnThreshold
name|long
name|getQueryWarnThreshold
parameter_list|()
block|{
return|return
name|queryWarnThreshold
return|;
block|}
DECL|method|getQueryInfoThreshold
name|long
name|getQueryInfoThreshold
parameter_list|()
block|{
return|return
name|queryInfoThreshold
return|;
block|}
DECL|method|getQueryDebugThreshold
name|long
name|getQueryDebugThreshold
parameter_list|()
block|{
return|return
name|queryDebugThreshold
return|;
block|}
DECL|method|getQueryTraceThreshold
name|long
name|getQueryTraceThreshold
parameter_list|()
block|{
return|return
name|queryTraceThreshold
return|;
block|}
DECL|method|getFetchWarnThreshold
name|long
name|getFetchWarnThreshold
parameter_list|()
block|{
return|return
name|fetchWarnThreshold
return|;
block|}
DECL|method|getFetchInfoThreshold
name|long
name|getFetchInfoThreshold
parameter_list|()
block|{
return|return
name|fetchInfoThreshold
return|;
block|}
DECL|method|getFetchDebugThreshold
name|long
name|getFetchDebugThreshold
parameter_list|()
block|{
return|return
name|fetchDebugThreshold
return|;
block|}
DECL|method|getFetchTraceThreshold
name|long
name|getFetchTraceThreshold
parameter_list|()
block|{
return|return
name|fetchTraceThreshold
return|;
block|}
DECL|method|getLevel
name|SlowLogLevel
name|getLevel
parameter_list|()
block|{
return|return
name|level
return|;
block|}
block|}
end_class

end_unit
