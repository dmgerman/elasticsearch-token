begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.hppc
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|hppc
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntIntOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntObjectOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectIntOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomStrings
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
name|unit
operator|.
name|SizeValue
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
name|IdentityHashMap
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
name|ThreadLocalRandom
import|;
end_import

begin_class
DECL|class|StringMapAdjustOrPutBenchmark
specifier|public
class|class
name|StringMapAdjustOrPutBenchmark
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
block|{
name|int
name|NUMBER_OF_KEYS
init|=
operator|(
name|int
operator|)
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"20"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
name|int
name|STRING_SIZE
init|=
literal|5
decl_stmt|;
name|long
name|PUT_OPERATIONS
init|=
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"5m"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
name|long
name|ITERATIONS
init|=
literal|10
decl_stmt|;
name|boolean
name|REUSE
init|=
literal|true
decl_stmt|;
name|String
index|[]
name|values
init|=
operator|new
name|String
index|[
name|NUMBER_OF_KEYS
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|RandomStrings
operator|.
name|randomAsciiOfLength
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
argument_list|,
name|STRING_SIZE
argument_list|)
expr_stmt|;
block|}
name|StopWatch
name|stopWatch
decl_stmt|;
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
name|map
init|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|map
operator|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|map
operator|.
name|addTo
argument_list|(
name|values
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
name|map
operator|=
literal|null
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"TObjectIntHashMap: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
comment|//        TObjectIntCustomHashMap<String> iMap = new TObjectIntCustomHashMap<String>(new StringIdentityHashingStrategy());
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
name|iMap
init|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|iMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|iMap
operator|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|iMap
operator|.
name|addTo
argument_list|(
name|values
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"TObjectIntCustomHashMap(StringIdentity): "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|iMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|iMap
operator|=
literal|null
expr_stmt|;
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|iMap
operator|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|iMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|iMap
operator|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|iMap
operator|.
name|addTo
argument_list|(
name|values
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"TObjectIntCustomHashMap(PureIdentity): "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|iMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|iMap
operator|=
literal|null
expr_stmt|;
comment|// now test with THashMap
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|StringEntry
argument_list|>
name|tMap
init|=
operator|new
name|ObjectObjectOpenHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|tMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|tMap
operator|=
operator|new
name|ObjectObjectOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|values
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
decl_stmt|;
name|StringEntry
name|stringEntry
init|=
name|tMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|stringEntry
operator|==
literal|null
condition|)
block|{
name|stringEntry
operator|=
operator|new
name|StringEntry
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|tMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|stringEntry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stringEntry
operator|.
name|counter
operator|++
expr_stmt|;
block|}
block|}
block|}
name|tMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tMap
operator|=
literal|null
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"THashMap: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|StringEntry
argument_list|>
name|hMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|hMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|hMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|values
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
decl_stmt|;
name|StringEntry
name|stringEntry
init|=
name|hMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|stringEntry
operator|==
literal|null
condition|)
block|{
name|stringEntry
operator|=
operator|new
name|StringEntry
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|stringEntry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stringEntry
operator|.
name|counter
operator|++
expr_stmt|;
block|}
block|}
block|}
name|hMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|hMap
operator|=
literal|null
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HashMap: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|IdentityHashMap
argument_list|<
name|String
argument_list|,
name|StringEntry
argument_list|>
name|ihMap
init|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|ihMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|hMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|values
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
decl_stmt|;
name|StringEntry
name|stringEntry
init|=
name|ihMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|stringEntry
operator|==
literal|null
condition|)
block|{
name|stringEntry
operator|=
operator|new
name|StringEntry
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|ihMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|stringEntry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stringEntry
operator|.
name|counter
operator|++
expr_stmt|;
block|}
block|}
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"IdentityHashMap: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|ihMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ihMap
operator|=
literal|null
expr_stmt|;
name|int
index|[]
name|iValues
init|=
operator|new
name|int
index|[
name|NUMBER_OF_KEYS
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|iValues
index|[
name|i
index|]
operator|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|IntIntOpenHashMap
name|intMap
init|=
operator|new
name|IntIntOpenHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|intMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|intMap
operator|=
operator|new
name|IntIntOpenHashMap
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|int
name|key
init|=
name|iValues
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
decl_stmt|;
name|intMap
operator|.
name|addTo
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"TIntIntHashMap: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|intMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|intMap
operator|=
literal|null
expr_stmt|;
comment|// now test with THashMap
name|stopWatch
operator|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|IntObjectOpenHashMap
argument_list|<
name|IntEntry
argument_list|>
name|tIntMap
init|=
operator|new
name|IntObjectOpenHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|iter
init|=
literal|0
init|;
name|iter
operator|<
name|ITERATIONS
condition|;
name|iter
operator|++
control|)
block|{
if|if
condition|(
name|REUSE
condition|)
block|{
name|tIntMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|tIntMap
operator|=
operator|new
name|IntObjectOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUT_OPERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|int
name|key
init|=
name|iValues
index|[
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|NUMBER_OF_KEYS
argument_list|)
index|]
decl_stmt|;
name|IntEntry
name|intEntry
init|=
name|tIntMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|intEntry
operator|==
literal|null
condition|)
block|{
name|intEntry
operator|=
operator|new
name|IntEntry
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|tIntMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|intEntry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|intEntry
operator|.
name|counter
operator|++
expr_stmt|;
block|}
block|}
block|}
name|tIntMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tIntMap
operator|=
literal|null
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"TIntObjectHashMap: "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millisFrac
argument_list|()
operator|/
name|ITERATIONS
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
DECL|class|StringEntry
specifier|static
class|class
name|StringEntry
block|{
DECL|field|key
name|String
name|key
decl_stmt|;
DECL|field|counter
name|int
name|counter
decl_stmt|;
DECL|method|StringEntry
name|StringEntry
parameter_list|(
name|String
name|key
parameter_list|,
name|int
name|counter
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
block|}
block|}
DECL|class|IntEntry
specifier|static
class|class
name|IntEntry
block|{
DECL|field|key
name|int
name|key
decl_stmt|;
DECL|field|counter
name|int
name|counter
decl_stmt|;
DECL|method|IntEntry
name|IntEntry
parameter_list|(
name|int
name|key
parameter_list|,
name|int
name|counter
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

