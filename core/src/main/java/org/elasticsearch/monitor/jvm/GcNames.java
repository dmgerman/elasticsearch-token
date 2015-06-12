begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.jvm
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
package|;
end_package

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|GcNames
specifier|public
class|class
name|GcNames
block|{
DECL|field|YOUNG
specifier|public
specifier|static
specifier|final
name|String
name|YOUNG
init|=
literal|"young"
decl_stmt|;
DECL|field|OLD
specifier|public
specifier|static
specifier|final
name|String
name|OLD
init|=
literal|"old"
decl_stmt|;
DECL|field|SURVIVOR
specifier|public
specifier|static
specifier|final
name|String
name|SURVIVOR
init|=
literal|"survivor"
decl_stmt|;
comment|/**      * Resolves the GC type by its memory pool name ({@link java.lang.management.MemoryPoolMXBean#getName()}.      */
DECL|method|getByMemoryPoolName
specifier|public
specifier|static
name|String
name|getByMemoryPoolName
parameter_list|(
name|String
name|poolName
parameter_list|,
name|String
name|defaultName
parameter_list|)
block|{
if|if
condition|(
literal|"Eden Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"PS Eden Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"Par Eden Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"G1 Eden Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
condition|)
block|{
return|return
name|YOUNG
return|;
block|}
if|if
condition|(
literal|"Survivor Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"PS Survivor Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"Par Survivor Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"G1 Survivor Space"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
condition|)
block|{
return|return
name|SURVIVOR
return|;
block|}
if|if
condition|(
literal|"Tenured Gen"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"PS Old Gen"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"CMS Old Gen"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
operator|||
literal|"G1 Old Gen"
operator|.
name|equals
argument_list|(
name|poolName
argument_list|)
condition|)
block|{
return|return
name|OLD
return|;
block|}
return|return
name|defaultName
return|;
block|}
DECL|method|getByGcName
specifier|public
specifier|static
name|String
name|getByGcName
parameter_list|(
name|String
name|gcName
parameter_list|,
name|String
name|defaultName
parameter_list|)
block|{
if|if
condition|(
literal|"Copy"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
operator|||
literal|"PS Scavenge"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
operator|||
literal|"ParNew"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
operator|||
literal|"G1 Young Generation"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
condition|)
block|{
return|return
name|YOUNG
return|;
block|}
if|if
condition|(
literal|"MarkSweepCompact"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
operator|||
literal|"PS MarkSweep"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
operator|||
literal|"ConcurrentMarkSweep"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
operator|||
literal|"G1 Old Generation"
operator|.
name|equals
argument_list|(
name|gcName
argument_list|)
condition|)
block|{
return|return
name|OLD
return|;
block|}
return|return
name|defaultName
return|;
block|}
block|}
end_class

end_unit
