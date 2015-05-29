begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support.filtering
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|filtering
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|XContent
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
name|json
operator|.
name|JsonXContent
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
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * Benchmark class to compare filtered and unfiltered XContent generators.  */
end_comment

begin_class
DECL|class|FilteringJsonGeneratorBenchmark
specifier|public
class|class
name|FilteringJsonGeneratorBenchmark
block|{
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|XContent
name|XCONTENT
init|=
name|JsonXContent
operator|.
name|jsonXContent
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Executing "
operator|+
name|FilteringJsonGeneratorBenchmark
operator|.
name|class
operator|+
literal|"..."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Warming up..."
argument_list|)
expr_stmt|;
name|run
argument_list|(
name|XCONTENT
argument_list|,
literal|500_000
argument_list|,
literal|100
argument_list|,
literal|0.5
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Warmed up."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"nb documents | nb fields | nb fields written | % fields written | time (millis) | rate (docs/sec) | avg size"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|nbFields
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|10
argument_list|,
literal|25
argument_list|,
literal|50
argument_list|,
literal|100
argument_list|,
literal|250
argument_list|)
control|)
block|{
for|for
control|(
name|int
name|nbDocs
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|,
literal|10_000
argument_list|,
literal|100_000
argument_list|,
literal|500_000
argument_list|)
control|)
block|{
for|for
control|(
name|double
name|ratio
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|0.0
argument_list|,
literal|1.0
argument_list|,
literal|0.99
argument_list|,
literal|0.95
argument_list|,
literal|0.9
argument_list|,
literal|0.75
argument_list|,
literal|0.5
argument_list|,
literal|0.25
argument_list|,
literal|0.1
argument_list|,
literal|0.05
argument_list|,
literal|0.01
argument_list|)
control|)
block|{
name|run
argument_list|(
name|XCONTENT
argument_list|,
name|nbDocs
argument_list|,
name|nbFields
argument_list|,
name|ratio
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Done."
argument_list|)
expr_stmt|;
block|}
DECL|method|run
specifier|private
specifier|static
name|void
name|run
parameter_list|(
name|XContent
name|xContent
parameter_list|,
name|long
name|nbIterations
parameter_list|,
name|int
name|nbFields
parameter_list|,
name|double
name|ratio
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|fields
init|=
name|fields
argument_list|(
name|nbFields
argument_list|)
decl_stmt|;
name|String
index|[]
name|filters
init|=
name|fields
argument_list|(
call|(
name|int
call|)
argument_list|(
name|nbFields
operator|*
name|ratio
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|size
init|=
literal|0
decl_stmt|;
name|BytesStreamOutput
name|os
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
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
name|nbIterations
condition|;
name|i
operator|++
control|)
block|{
name|XContentBuilder
name|builder
init|=
operator|new
name|XContentBuilder
argument_list|(
name|xContent
argument_list|,
name|os
argument_list|,
name|filters
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|field
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|size
operator|+=
name|builder
operator|.
name|bytes
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
name|os
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|double
name|milliseconds
init|=
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
operator|)
operator|/
literal|1_000_000d
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%12d | %9d | %17d | %14.2f %% | %10.3f ms | %15.2f | %8.0f %n"
argument_list|,
name|nbIterations
argument_list|,
name|nbFields
argument_list|,
call|(
name|int
call|)
argument_list|(
name|nbFields
operator|*
name|ratio
argument_list|)
argument_list|,
operator|(
name|ratio
operator|*
literal|100d
operator|)
argument_list|,
name|milliseconds
argument_list|,
operator|(
operator|(
name|double
operator|)
name|nbIterations
operator|)
operator|/
operator|(
name|milliseconds
operator|/
literal|1000d
operator|)
argument_list|,
name|size
operator|/
operator|(
operator|(
name|double
operator|)
name|nbIterations
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns a String array of field names starting from "field_0" with a length of n.      * If n=3, the array is ["field_0","field_1","field_2"]      */
DECL|method|fields
specifier|private
specifier|static
name|String
index|[]
name|fields
parameter_list|(
name|int
name|n
parameter_list|)
block|{
name|String
index|[]
name|fields
init|=
operator|new
name|String
index|[
name|n
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|fields
index|[
name|i
index|]
operator|=
literal|"field_"
operator|+
name|i
expr_stmt|;
block|}
return|return
name|fields
return|;
block|}
block|}
end_class

end_unit
