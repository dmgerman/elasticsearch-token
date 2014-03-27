begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.scripts.score
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|scripts
operator|.
name|score
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
operator|.
name|BulkRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|StopWatch
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|CombineFunction
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
name|ImmutableSettings
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|FilterBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|script
operator|.
name|ScriptScoreFunctionBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|searchRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|functionScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilders
operator|.
name|scriptFunction
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
operator|.
name|searchSource
import|;
end_import

begin_class
DECL|class|BasicScriptBenchmark
specifier|public
class|class
name|BasicScriptBenchmark
block|{
DECL|class|RequestInfo
specifier|public
specifier|static
class|class
name|RequestInfo
block|{
DECL|method|RequestInfo
specifier|public
name|RequestInfo
parameter_list|(
name|SearchRequest
name|source
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|request
operator|=
name|source
expr_stmt|;
name|numTerms
operator|=
name|i
expr_stmt|;
block|}
DECL|field|request
name|SearchRequest
name|request
decl_stmt|;
DECL|field|numTerms
name|int
name|numTerms
decl_stmt|;
block|}
DECL|class|Results
specifier|public
specifier|static
class|class
name|Results
block|{
DECL|field|TIME_PER_DOCIN_MILLIS
specifier|public
specifier|static
specifier|final
name|String
name|TIME_PER_DOCIN_MILLIS
init|=
literal|"timePerDocinMillis"
decl_stmt|;
DECL|field|NUM_TERMS
specifier|public
specifier|static
specifier|final
name|String
name|NUM_TERMS
init|=
literal|"numTerms"
decl_stmt|;
DECL|field|NUM_DOCS
specifier|public
specifier|static
specifier|final
name|String
name|NUM_DOCS
init|=
literal|"numDocs"
decl_stmt|;
DECL|field|TIME_PER_QUERY_IN_SEC
specifier|public
specifier|static
specifier|final
name|String
name|TIME_PER_QUERY_IN_SEC
init|=
literal|"timePerQueryInSec"
decl_stmt|;
DECL|field|TOTAL_TIME_IN_SEC
specifier|public
specifier|static
specifier|final
name|String
name|TOTAL_TIME_IN_SEC
init|=
literal|"totalTimeInSec"
decl_stmt|;
DECL|field|resultSeconds
name|Double
index|[]
name|resultSeconds
decl_stmt|;
DECL|field|resultMSPerQuery
name|Double
index|[]
name|resultMSPerQuery
decl_stmt|;
DECL|field|numDocs
name|Long
index|[]
name|numDocs
decl_stmt|;
DECL|field|numTerms
name|Integer
index|[]
name|numTerms
decl_stmt|;
DECL|field|timePerDoc
name|Double
index|[]
name|timePerDoc
decl_stmt|;
DECL|field|label
name|String
name|label
decl_stmt|;
DECL|field|description
name|String
name|description
decl_stmt|;
DECL|field|lineStyle
specifier|public
name|String
name|lineStyle
decl_stmt|;
DECL|field|color
specifier|public
name|String
name|color
decl_stmt|;
DECL|method|init
name|void
name|init
parameter_list|(
name|int
name|numVariations
parameter_list|,
name|String
name|label
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|color
parameter_list|,
name|String
name|lineStyle
parameter_list|)
block|{
name|resultSeconds
operator|=
operator|new
name|Double
index|[
name|numVariations
index|]
expr_stmt|;
name|resultMSPerQuery
operator|=
operator|new
name|Double
index|[
name|numVariations
index|]
expr_stmt|;
name|numDocs
operator|=
operator|new
name|Long
index|[
name|numVariations
index|]
expr_stmt|;
name|numTerms
operator|=
operator|new
name|Integer
index|[
name|numVariations
index|]
expr_stmt|;
name|timePerDoc
operator|=
operator|new
name|Double
index|[
name|numVariations
index|]
expr_stmt|;
name|this
operator|.
name|label
operator|=
name|label
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|color
operator|=
name|color
expr_stmt|;
name|this
operator|.
name|lineStyle
operator|=
name|lineStyle
expr_stmt|;
block|}
DECL|method|set
name|void
name|set
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|StopWatch
name|stopWatch
parameter_list|,
name|String
name|message
parameter_list|,
name|int
name|maxIter
parameter_list|,
name|int
name|which
parameter_list|,
name|int
name|numTerms
parameter_list|)
block|{
name|resultSeconds
index|[
name|which
index|]
operator|=
call|(
name|double
call|)
argument_list|(
operator|(
name|double
operator|)
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|.
name|getMillis
argument_list|()
operator|/
operator|(
name|double
operator|)
literal|1000
argument_list|)
expr_stmt|;
name|resultMSPerQuery
index|[
name|which
index|]
operator|=
call|(
name|double
call|)
argument_list|(
operator|(
name|double
operator|)
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
operator|/
operator|(
name|double
operator|)
name|maxIter
argument_list|)
expr_stmt|;
name|numDocs
index|[
name|which
index|]
operator|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
expr_stmt|;
name|this
operator|.
name|numTerms
index|[
name|which
index|]
operator|=
name|numTerms
expr_stmt|;
name|timePerDoc
index|[
name|which
index|]
operator|=
name|resultMSPerQuery
index|[
name|which
index|]
operator|/
name|numDocs
index|[
name|which
index|]
expr_stmt|;
block|}
DECL|method|printResults
specifier|public
name|void
name|printResults
parameter_list|(
name|BufferedWriter
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|comma
init|=
operator|(
name|writer
operator|==
literal|null
operator|)
condition|?
literal|""
else|:
literal|";"
decl_stmt|;
name|String
name|results
init|=
name|description
operator|+
literal|"\n"
operator|+
name|Results
operator|.
name|TOTAL_TIME_IN_SEC
operator|+
literal|" = "
operator|+
name|getResultArray
argument_list|(
name|resultSeconds
argument_list|)
operator|+
name|comma
operator|+
literal|"\n"
operator|+
name|Results
operator|.
name|TIME_PER_QUERY_IN_SEC
operator|+
literal|" = "
operator|+
name|getResultArray
argument_list|(
name|resultMSPerQuery
argument_list|)
operator|+
name|comma
operator|+
literal|"\n"
operator|+
name|Results
operator|.
name|NUM_DOCS
operator|+
literal|" = "
operator|+
name|getResultArray
argument_list|(
name|numDocs
argument_list|)
operator|+
name|comma
operator|+
literal|"\n"
operator|+
name|Results
operator|.
name|NUM_TERMS
operator|+
literal|" = "
operator|+
name|getResultArray
argument_list|(
name|numTerms
argument_list|)
operator|+
name|comma
operator|+
literal|"\n"
operator|+
name|Results
operator|.
name|TIME_PER_DOCIN_MILLIS
operator|+
literal|" = "
operator|+
name|getResultArray
argument_list|(
name|timePerDoc
argument_list|)
operator|+
name|comma
operator|+
literal|"\n"
decl_stmt|;
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getResultArray
specifier|private
name|String
name|getResultArray
parameter_list|(
name|Object
index|[]
name|resultArray
parameter_list|)
block|{
name|String
name|result
init|=
literal|"["
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|resultArray
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|+=
name|resultArray
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|!=
name|resultArray
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|result
operator|+=
literal|","
expr_stmt|;
block|}
block|}
name|result
operator|+=
literal|"]"
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
DECL|method|BasicScriptBenchmark
specifier|public
name|BasicScriptBenchmark
parameter_list|()
block|{     }
DECL|field|termsList
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|termsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|init
specifier|static
name|void
name|init
parameter_list|(
name|int
name|numTerms
parameter_list|)
block|{
name|SecureRandom
name|random
init|=
operator|new
name|SecureRandom
argument_list|()
decl_stmt|;
name|random
operator|.
name|setSeed
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|termsList
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numTerms
condition|;
name|i
operator|++
control|)
block|{
name|String
name|term
init|=
operator|new
name|BigInteger
argument_list|(
literal|512
argument_list|,
name|random
argument_list|)
operator|.
name|toString
argument_list|(
literal|32
argument_list|)
decl_stmt|;
name|termsList
operator|.
name|add
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getTerms
specifier|static
name|String
index|[]
name|getTerms
parameter_list|(
name|int
name|numTerms
parameter_list|)
block|{
name|String
index|[]
name|terms
init|=
operator|new
name|String
index|[
name|numTerms
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numTerms
condition|;
name|i
operator|++
control|)
block|{
name|terms
index|[
name|i
index|]
operator|=
name|termsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
DECL|method|writeHelperFunction
specifier|public
specifier|static
name|void
name|writeHelperFunction
parameter_list|()
throws|throws
name|IOException
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
literal|"addToPlot.m"
argument_list|)
decl_stmt|;
name|BufferedWriter
name|out
init|=
name|Files
operator|.
name|newWriter
argument_list|(
name|file
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"function handle = addToPlot(numTerms, perDoc, color, linestyle, linewidth)\n"
operator|+
literal|"handle = line(numTerms, perDoc);\n"
operator|+
literal|"set(handle, 'color', color);\n"
operator|+
literal|"set(handle, 'linestyle',linestyle);\n"
operator|+
literal|"set(handle, 'LineWidth',linewidth);\n"
operator|+
literal|"end\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|printOctaveScript
specifier|public
specifier|static
name|void
name|printOctaveScript
parameter_list|(
name|List
argument_list|<
name|Results
argument_list|>
name|allResults
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|BufferedWriter
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|out
operator|=
name|Files
operator|.
name|newWriter
argument_list|(
name|file
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"#! /usr/local/bin/octave -qf"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"\n\n\n\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"######################################\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"# Octave script for plotting results\n"
argument_list|)
expr_stmt|;
name|String
name|filename
init|=
literal|"scriptScoreBenchmark"
operator|+
operator|new
name|DateTime
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"#Call '"
operator|+
name|args
index|[
literal|0
index|]
operator|+
literal|"' from the command line. The plot is then in "
operator|+
name|filename
operator|+
literal|"\n\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"handleArray = [];\n tagArray = [];\n plot([]);\n hold on;\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|Results
name|result
range|:
name|allResults
control|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"# "
operator|+
name|result
operator|.
name|description
argument_list|)
expr_stmt|;
name|result
operator|.
name|printResults
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"handleArray = [handleArray, addToPlot("
operator|+
name|Results
operator|.
name|NUM_TERMS
operator|+
literal|", "
operator|+
name|Results
operator|.
name|TIME_PER_DOCIN_MILLIS
operator|+
literal|", '"
operator|+
name|result
operator|.
name|color
operator|+
literal|"','"
operator|+
name|result
operator|.
name|lineStyle
operator|+
literal|"',5)];\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"tagArray = [tagArray; '"
operator|+
name|result
operator|.
name|label
operator|+
literal|"'];\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|write
argument_list|(
literal|"xlabel(\'number of query terms');"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"ylabel(\'query time per document');"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"legend(handleArray,tagArray);\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"saveas(gcf,'"
operator|+
name|filename
operator|+
literal|".png','png')\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"hold off;\n\n"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|writeHelperFunction
argument_list|()
expr_stmt|;
block|}
DECL|method|printResult
specifier|static
name|void
name|printResult
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|StopWatch
name|stopWatch
parameter_list|,
name|String
name|queryInfo
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Searching with "
operator|+
name|queryInfo
operator|+
literal|" took "
operator|+
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|+
literal|", per query "
operator|+
operator|(
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
operator|/
literal|100
operator|)
operator|+
literal|" for "
operator|+
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|+
literal|" docs"
argument_list|)
expr_stmt|;
block|}
DECL|method|indexData
specifier|static
name|void
name|indexData
parameter_list|(
name|long
name|numDocs
parameter_list|,
name|Client
name|client
parameter_list|,
name|boolean
name|randomizeTerms
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// index might exist already, in this case we do nothing TODO: make
comment|// saver in general
block|}
name|XContentBuilder
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"text"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index_options"
argument_list|,
literal|"offsets"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"payload_float"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|mapping
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.payload_float.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.payload_float.filter"
argument_list|,
literal|"delimited_float"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.delimited_float.delimiter"
argument_list|,
literal|"|"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.delimited_float.encoding"
argument_list|,
literal|"float"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.delimited_float.type"
argument_list|,
literal|"delimited_payload_filter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"10s"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|BulkRequestBuilder
name|bulkRequest
init|=
name|client
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|bulkRequest
operator|.
name|add
argument_list|(
name|client
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
name|randomText
argument_list|(
name|random
argument_list|,
name|randomizeTerms
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|bulkRequest
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|bulkRequest
operator|=
name|client
operator|.
name|prepareBulk
argument_list|()
expr_stmt|;
block|}
block|}
name|bulkRequest
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setFull
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Done indexing "
operator|+
name|numDocs
operator|+
literal|" documents"
argument_list|)
expr_stmt|;
block|}
DECL|method|randomText
specifier|private
specifier|static
name|String
name|randomText
parameter_list|(
name|Random
name|random
parameter_list|,
name|boolean
name|randomizeTerms
parameter_list|)
block|{
name|String
name|text
init|=
literal|""
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|termsList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
operator|==
literal|3
operator|||
operator|!
name|randomizeTerms
condition|)
block|{
name|text
operator|=
name|text
operator|+
literal|" "
operator|+
name|termsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"|1"
expr_stmt|;
block|}
block|}
return|return
name|text
return|;
block|}
DECL|method|printTimings
specifier|static
name|void
name|printTimings
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|StopWatch
name|stopWatch
parameter_list|,
name|String
name|message
parameter_list|,
name|int
name|maxIter
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|+
literal|", "
operator|+
operator|(
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
operator|/
name|maxIter
operator|)
operator|+
literal|", "
operator|+
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|+
literal|", "
operator|+
operator|(
name|stopWatch
operator|.
name|lastTaskTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
operator|/
operator|(
name|maxIter
operator|+
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|)
operator|)
argument_list|)
expr_stmt|;
block|}
DECL|method|initTermQueries
specifier|static
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|initTermQueries
parameter_list|(
name|int
name|minTerms
parameter_list|,
name|int
name|maxTerms
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|termSearchRequests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|nTerms
init|=
name|minTerms
init|;
name|nTerms
operator|<
name|maxTerms
condition|;
name|nTerms
operator|++
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|terms
init|=
name|getTerms
argument_list|(
name|nTerms
operator|+
literal|1
argument_list|)
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"text"
argument_list|,
name|terms
argument_list|)
expr_stmt|;
name|SearchRequest
name|request
init|=
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|false
argument_list|)
operator|.
name|size
argument_list|(
literal|0
argument_list|)
operator|.
name|query
argument_list|(
name|QueryBuilders
operator|.
name|termsQuery
argument_list|(
literal|"text"
argument_list|,
name|terms
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|infoString
init|=
literal|"Results for term query with "
operator|+
operator|(
name|nTerms
operator|+
literal|1
operator|)
operator|+
literal|" terms:"
decl_stmt|;
name|termSearchRequests
operator|.
name|add
argument_list|(
operator|new
name|AbstractMap
operator|.
name|SimpleEntry
argument_list|<>
argument_list|(
name|infoString
argument_list|,
operator|new
name|RequestInfo
argument_list|(
name|request
argument_list|,
name|nTerms
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|termSearchRequests
return|;
block|}
DECL|method|initNativeSearchRequests
specifier|static
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|initNativeSearchRequests
parameter_list|(
name|int
name|minTerms
parameter_list|,
name|int
name|maxTerms
parameter_list|,
name|String
name|script
parameter_list|,
name|boolean
name|langNative
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|nativeSearchRequests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|nTerms
init|=
name|minTerms
init|;
name|nTerms
operator|<
name|maxTerms
condition|;
name|nTerms
operator|++
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|terms
init|=
name|getTerms
argument_list|(
name|nTerms
operator|+
literal|1
argument_list|)
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"text"
argument_list|,
name|terms
argument_list|)
expr_stmt|;
name|String
name|infoString
init|=
literal|"Results for native script with "
operator|+
operator|(
name|nTerms
operator|+
literal|1
operator|)
operator|+
literal|" terms:"
decl_stmt|;
name|ScriptScoreFunctionBuilder
name|scriptFunction
init|=
operator|(
name|langNative
operator|==
literal|true
operator|)
condition|?
name|scriptFunction
argument_list|(
name|script
argument_list|,
literal|"native"
argument_list|,
name|params
argument_list|)
else|:
name|scriptFunction
argument_list|(
name|script
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|SearchRequest
name|request
init|=
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|false
argument_list|)
operator|.
name|size
argument_list|(
literal|0
argument_list|)
operator|.
name|query
argument_list|(
name|functionScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|termsFilter
argument_list|(
literal|"text"
argument_list|,
name|terms
argument_list|)
argument_list|,
name|scriptFunction
argument_list|)
operator|.
name|boostMode
argument_list|(
name|CombineFunction
operator|.
name|REPLACE
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|nativeSearchRequests
operator|.
name|add
argument_list|(
operator|new
name|AbstractMap
operator|.
name|SimpleEntry
argument_list|<>
argument_list|(
name|infoString
argument_list|,
operator|new
name|RequestInfo
argument_list|(
name|request
argument_list|,
name|nTerms
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|nativeSearchRequests
return|;
block|}
DECL|method|initScriptMatchAllSearchRequests
specifier|static
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|initScriptMatchAllSearchRequests
parameter_list|(
name|String
name|script
parameter_list|,
name|boolean
name|langNative
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|nativeSearchRequests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|infoString
init|=
literal|"Results for constant score script:"
decl_stmt|;
name|ScriptScoreFunctionBuilder
name|scriptFunction
init|=
operator|(
name|langNative
operator|==
literal|true
operator|)
condition|?
name|scriptFunction
argument_list|(
name|script
argument_list|,
literal|"native"
argument_list|)
else|:
name|scriptFunction
argument_list|(
name|script
argument_list|)
decl_stmt|;
name|SearchRequest
name|request
init|=
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|false
argument_list|)
operator|.
name|size
argument_list|(
literal|0
argument_list|)
operator|.
name|query
argument_list|(
name|functionScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|matchAllFilter
argument_list|()
argument_list|,
name|scriptFunction
argument_list|)
operator|.
name|boostMode
argument_list|(
name|CombineFunction
operator|.
name|REPLACE
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|nativeSearchRequests
operator|.
name|add
argument_list|(
operator|new
name|AbstractMap
operator|.
name|SimpleEntry
argument_list|<>
argument_list|(
name|infoString
argument_list|,
operator|new
name|RequestInfo
argument_list|(
name|request
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|nativeSearchRequests
return|;
block|}
DECL|method|runBenchmark
specifier|static
name|void
name|runBenchmark
parameter_list|(
name|Client
name|client
parameter_list|,
name|int
name|maxIter
parameter_list|,
name|Results
name|results
parameter_list|,
name|List
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
argument_list|>
name|nativeSearchRequests
parameter_list|,
name|int
name|minTerms
parameter_list|,
name|int
name|warmerIter
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|RequestInfo
argument_list|>
name|entry
range|:
name|nativeSearchRequests
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
literal|null
decl_stmt|;
comment|// warm up
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|warmerIter
condition|;
name|i
operator|++
control|)
block|{
name|searchResponse
operator|=
name|client
operator|.
name|search
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|request
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
comment|// run benchmark
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
decl_stmt|;
name|stopWatch
operator|.
name|start
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxIter
condition|;
name|i
operator|++
control|)
block|{
name|searchResponse
operator|=
name|client
operator|.
name|search
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|request
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|results
operator|.
name|set
argument_list|(
name|searchResponse
argument_list|,
name|stopWatch
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|maxIter
argument_list|,
name|counter
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|numTerms
argument_list|)
expr_stmt|;
name|counter
operator|++
expr_stmt|;
block|}
name|results
operator|.
name|printResults
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

