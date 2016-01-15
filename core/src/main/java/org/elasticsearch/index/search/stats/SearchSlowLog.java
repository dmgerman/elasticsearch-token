begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.stats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|stats
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
name|Settings
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
name|Locale
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
name|String
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
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.warn"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.info"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.debug"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.query.trace"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.warn"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.info"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.debug"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".threshold.fetch.trace"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_REFORMAT
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_REFORMAT
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".reformat"
decl_stmt|;
DECL|field|INDEX_SEARCH_SLOWLOG_LEVEL
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_SEARCH_SLOWLOG_LEVEL
init|=
name|INDEX_SEARCH_SLOWLOG_PREFIX
operator|+
literal|".level"
decl_stmt|;
DECL|method|SearchSlowLog
specifier|public
name|SearchSlowLog
parameter_list|(
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
operator|.
name|reformat
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_SEARCH_SLOWLOG_REFORMAT
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryWarnThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|queryInfoThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|queryDebugThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|queryTraceThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|fetchWarnThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|fetchInfoThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|fetchDebugThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|fetchTraceThreshold
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
expr_stmt|;
name|this
operator|.
name|level
operator|=
name|indexSettings
operator|.
name|get
argument_list|(
name|INDEX_SEARCH_SLOWLOG_LEVEL
argument_list|,
literal|"TRACE"
argument_list|)
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
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
name|queryLogger
operator|.
name|setLevel
argument_list|(
name|level
argument_list|)
expr_stmt|;
name|fetchLogger
operator|.
name|setLevel
argument_list|(
name|level
argument_list|)
expr_stmt|;
block|}
DECL|method|onQueryPhase
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
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|long
name|queryWarnThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|queryWarnThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryWarnThreshold
operator|!=
name|this
operator|.
name|queryWarnThreshold
condition|)
block|{
name|this
operator|.
name|queryWarnThreshold
operator|=
name|queryWarnThreshold
expr_stmt|;
block|}
name|long
name|queryInfoThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|queryInfoThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryInfoThreshold
operator|!=
name|this
operator|.
name|queryInfoThreshold
condition|)
block|{
name|this
operator|.
name|queryInfoThreshold
operator|=
name|queryInfoThreshold
expr_stmt|;
block|}
name|long
name|queryDebugThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|queryDebugThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryDebugThreshold
operator|!=
name|this
operator|.
name|queryDebugThreshold
condition|)
block|{
name|this
operator|.
name|queryDebugThreshold
operator|=
name|queryDebugThreshold
expr_stmt|;
block|}
name|long
name|queryTraceThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|queryTraceThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTraceThreshold
operator|!=
name|this
operator|.
name|queryTraceThreshold
condition|)
block|{
name|this
operator|.
name|queryTraceThreshold
operator|=
name|queryTraceThreshold
expr_stmt|;
block|}
name|long
name|fetchWarnThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|fetchWarnThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchWarnThreshold
operator|!=
name|this
operator|.
name|fetchWarnThreshold
condition|)
block|{
name|this
operator|.
name|fetchWarnThreshold
operator|=
name|fetchWarnThreshold
expr_stmt|;
block|}
name|long
name|fetchInfoThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|fetchInfoThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchInfoThreshold
operator|!=
name|this
operator|.
name|fetchInfoThreshold
condition|)
block|{
name|this
operator|.
name|fetchInfoThreshold
operator|=
name|fetchInfoThreshold
expr_stmt|;
block|}
name|long
name|fetchDebugThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|fetchDebugThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchDebugThreshold
operator|!=
name|this
operator|.
name|fetchDebugThreshold
condition|)
block|{
name|this
operator|.
name|fetchDebugThreshold
operator|=
name|fetchDebugThreshold
expr_stmt|;
block|}
name|long
name|fetchTraceThreshold
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|this
operator|.
name|fetchTraceThreshold
argument_list|)
argument_list|)
operator|.
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchTraceThreshold
operator|!=
name|this
operator|.
name|fetchTraceThreshold
condition|)
block|{
name|this
operator|.
name|fetchTraceThreshold
operator|=
name|fetchTraceThreshold
expr_stmt|;
block|}
name|String
name|level
init|=
name|settings
operator|.
name|get
argument_list|(
name|INDEX_SEARCH_SLOWLOG_LEVEL
argument_list|,
name|this
operator|.
name|level
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|level
operator|.
name|equals
argument_list|(
name|this
operator|.
name|level
argument_list|)
condition|)
block|{
name|this
operator|.
name|queryLogger
operator|.
name|setLevel
argument_list|(
name|level
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
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
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|level
operator|=
name|level
expr_stmt|;
block|}
name|boolean
name|reformat
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_SEARCH_SLOWLOG_REFORMAT
argument_list|,
name|this
operator|.
name|reformat
argument_list|)
decl_stmt|;
if|if
condition|(
name|reformat
operator|!=
name|this
operator|.
name|reformat
condition|)
block|{
name|this
operator|.
name|reformat
operator|=
name|reformat
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
name|types
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
name|types
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
block|}
end_class

end_unit

