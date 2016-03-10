begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CloseableThreadLocal
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
name|collect
operator|.
name|Iterators
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Collections
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
name|Map
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * A ThreadContext is a map of string headers and a transient map of keyed objects that are associated with  * a thread. It allows to store and retrieve header information across method calls, network calls as well as threads spawned from a  * thread that has a {@link ThreadContext} associated with. Threads spawned from a {@link org.elasticsearch.threadpool.ThreadPool} have out of the box  * support for {@link ThreadContext} and all threads spawned will inherit the {@link ThreadContext} from the thread that it is forking from.".  * Network calls will also preserve the senders headers automatically.  *<p>  * Consumers of ThreadContext usually don't need to interact with adding or stashing contexts. Every elasticsearch thread is managed by a thread pool or executor  * being responsible for stashing and restoring the threads context. For instance if a network request is received, all headers are deserialized from the network  * and directly added as the headers of the threads {@link ThreadContext} (see {@link #readHeaders(StreamInput)}. In order to not modify the context that is currently  * active on this thread the network code uses a try/with pattern to stash it's current context, read headers into a fresh one and once the request is handled or a handler thread  * is forked (which in turn inherits the context) it restores the previous context. For instance:  *</p>  *<pre>  *     // current context is stashed and replaced with a default context  *     try (StoredContext context = threadContext.stashContext()) {  *         threadContext.readHeaders(in); // read headers into current context  *         if (fork) {  *             threadPool.execute(() -&gt; request.handle()); // inherits context  *         } else {  *             request.handle();  *         }  *     }  *     // previous context is restored on StoredContext#close()  *</pre>  *  */
end_comment

begin_class
DECL|class|ThreadContext
specifier|public
specifier|final
class|class
name|ThreadContext
implements|implements
name|Closeable
implements|,
name|Writeable
argument_list|<
name|ThreadContext
operator|.
name|ThreadContextStruct
argument_list|>
block|{
DECL|field|PREFIX
specifier|public
specifier|static
specifier|final
name|String
name|PREFIX
init|=
literal|"request.headers"
decl_stmt|;
DECL|field|DEFAULT_HEADERS_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Settings
argument_list|>
name|DEFAULT_HEADERS_SETTING
init|=
name|Setting
operator|.
name|groupSetting
argument_list|(
name|PREFIX
operator|+
literal|"."
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|defaultHeader
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|defaultHeader
decl_stmt|;
DECL|field|DEFAULT_CONTEXT
specifier|private
specifier|static
specifier|final
name|ThreadContextStruct
name|DEFAULT_CONTEXT
init|=
operator|new
name|ThreadContextStruct
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|threadLocal
specifier|private
specifier|final
name|ContextThreadLocal
name|threadLocal
decl_stmt|;
comment|/**      * Creates a new ThreadContext instance      * @param settings the settings to read the default request headers from      */
DECL|method|ThreadContext
specifier|public
name|ThreadContext
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|Settings
name|headers
init|=
name|DEFAULT_HEADERS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|headers
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|defaultHeader
operator|=
name|Collections
operator|.
name|emptyMap
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|defaultHeader
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|headers
operator|.
name|names
argument_list|()
control|)
block|{
name|defaultHeader
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|headers
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|defaultHeader
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|defaultHeader
argument_list|)
expr_stmt|;
block|}
name|threadLocal
operator|=
operator|new
name|ContextThreadLocal
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|threadLocal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**      * Removes the current context and resets a default context. The removed context can be      * restored when closing the returned {@link StoredContext}      */
DECL|method|stashContext
specifier|public
name|StoredContext
name|stashContext
parameter_list|()
block|{
specifier|final
name|ThreadContextStruct
name|context
init|=
name|threadLocal
operator|.
name|get
argument_list|()
decl_stmt|;
name|threadLocal
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
parameter_list|()
lambda|->
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
return|;
block|}
comment|/**      * Removes the current context and resets a new context that contains a merge of the current headers and the given headers. The removed context can be      * restored when closing the returned {@link StoredContext}. The merge strategy is that headers that are already existing are preserved unless they are defaults.      */
DECL|method|stashAndMergeHeaders
specifier|public
name|StoredContext
name|stashAndMergeHeaders
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
parameter_list|)
block|{
specifier|final
name|ThreadContextStruct
name|context
init|=
name|threadLocal
operator|.
name|get
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newHeader
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|headers
argument_list|)
decl_stmt|;
name|newHeader
operator|.
name|putAll
argument_list|(
name|context
operator|.
name|headers
argument_list|)
expr_stmt|;
name|threadLocal
operator|.
name|set
argument_list|(
name|DEFAULT_CONTEXT
operator|.
name|putHeaders
argument_list|(
name|newHeader
argument_list|)
argument_list|)
expr_stmt|;
return|return
parameter_list|()
lambda|->
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
return|;
block|}
comment|/**      * Just like {@link #stashContext()} but no default context is set.      */
DECL|method|newStoredContext
specifier|public
name|StoredContext
name|newStoredContext
parameter_list|()
block|{
specifier|final
name|ThreadContextStruct
name|context
init|=
name|threadLocal
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
parameter_list|()
lambda|->
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|writeTo
argument_list|(
name|out
argument_list|,
name|defaultHeader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|ThreadContextStruct
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DEFAULT_CONTEXT
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
comment|/**      * Reads the headers from the stream into the current context      */
DECL|method|readHeaders
specifier|public
name|void
name|readHeaders
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the header for the given key or<code>null</code> if not present      */
DECL|method|getHeader
specifier|public
name|String
name|getHeader
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|String
name|value
init|=
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|headers
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|defaultHeader
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
return|return
name|value
return|;
block|}
comment|/**      * Returns all of the current contexts headers      */
DECL|method|getHeaders
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHeaders
parameter_list|()
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|defaultHeader
argument_list|)
decl_stmt|;
name|map
operator|.
name|putAll
argument_list|(
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|headers
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
return|;
block|}
comment|/**      * Copies all header key, value pairs into the current context      */
DECL|method|copyHeaders
specifier|public
name|void
name|copyHeaders
parameter_list|(
name|Iterable
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|headers
parameter_list|)
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|copyHeaders
argument_list|(
name|headers
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Puts a header into the context      */
DECL|method|putHeader
specifier|public
name|void
name|putHeader
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|putPersistent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Puts all of the given headers into this context      */
DECL|method|putHeader
specifier|public
name|void
name|putHeader
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|header
parameter_list|)
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|putHeaders
argument_list|(
name|header
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Puts a transient header object into this context      */
DECL|method|putTransient
specifier|public
name|void
name|putTransient
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|threadLocal
operator|.
name|set
argument_list|(
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|putTransient
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns a transient header object or<code>null</code> if there is no header for the given key      */
DECL|method|getTransient
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getTransient
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
operator|(
name|T
operator|)
name|threadLocal
operator|.
name|get
argument_list|()
operator|.
name|transientHeaders
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
DECL|interface|StoredContext
specifier|public
interface|interface
name|StoredContext
extends|extends
name|AutoCloseable
block|{
annotation|@
name|Override
DECL|method|close
name|void
name|close
parameter_list|()
function_decl|;
DECL|method|restore
specifier|default
name|void
name|restore
parameter_list|()
block|{
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|ThreadContextStruct
specifier|static
specifier|final
class|class
name|ThreadContextStruct
implements|implements
name|Writeable
argument_list|<
name|ThreadContextStruct
argument_list|>
block|{
DECL|field|headers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
decl_stmt|;
DECL|field|transientHeaders
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|transientHeaders
decl_stmt|;
DECL|method|ThreadContextStruct
specifier|private
name|ThreadContextStruct
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|numValues
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
init|=
name|numValues
operator|==
literal|0
condition|?
name|Collections
operator|.
name|emptyMap
argument_list|()
else|:
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|numValues
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
name|numValues
condition|;
name|i
operator|++
control|)
block|{
name|headers
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
name|this
operator|.
name|transientHeaders
operator|=
name|Collections
operator|.
name|emptyMap
argument_list|()
expr_stmt|;
block|}
DECL|method|ThreadContextStruct
specifier|private
name|ThreadContextStruct
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|transientHeaders
parameter_list|)
block|{
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
name|this
operator|.
name|transientHeaders
operator|=
name|transientHeaders
expr_stmt|;
block|}
DECL|method|ThreadContextStruct
specifier|private
name|ThreadContextStruct
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
parameter_list|)
block|{
name|this
argument_list|(
name|headers
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|putPersistent
specifier|private
name|ThreadContextStruct
name|putPersistent
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newHeaders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|this
operator|.
name|headers
argument_list|)
decl_stmt|;
name|putSingleHeader
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|newHeaders
argument_list|)
expr_stmt|;
return|return
operator|new
name|ThreadContextStruct
argument_list|(
name|newHeaders
argument_list|,
name|transientHeaders
argument_list|)
return|;
block|}
DECL|method|putSingleHeader
specifier|private
name|void
name|putSingleHeader
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newHeaders
parameter_list|)
block|{
specifier|final
name|String
name|existingValue
decl_stmt|;
if|if
condition|(
operator|(
name|existingValue
operator|=
name|newHeaders
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"value for key ["
operator|+
name|key
operator|+
literal|"] already present"
argument_list|)
throw|;
block|}
block|}
DECL|method|putHeaders
specifier|private
name|ThreadContextStruct
name|putHeaders
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
parameter_list|)
block|{
if|if
condition|(
name|headers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
else|else
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newHeaders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|headers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|putSingleHeader
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|newHeaders
argument_list|)
expr_stmt|;
block|}
name|newHeaders
operator|.
name|putAll
argument_list|(
name|this
operator|.
name|headers
argument_list|)
expr_stmt|;
return|return
operator|new
name|ThreadContextStruct
argument_list|(
name|newHeaders
argument_list|,
name|transientHeaders
argument_list|)
return|;
block|}
block|}
DECL|method|putTransient
specifier|private
name|ThreadContextStruct
name|putTransient
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|newTransient
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|this
operator|.
name|transientHeaders
argument_list|)
decl_stmt|;
if|if
condition|(
name|newTransient
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"value for key ["
operator|+
name|key
operator|+
literal|"] already present"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ThreadContextStruct
argument_list|(
name|headers
argument_list|,
name|newTransient
argument_list|)
return|;
block|}
DECL|method|isEmpty
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|headers
operator|.
name|isEmpty
argument_list|()
operator|&&
name|transientHeaders
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|copyHeaders
specifier|private
name|ThreadContextStruct
name|copyHeaders
parameter_list|(
name|Iterable
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|headers
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newHeaders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|header
range|:
name|headers
control|)
block|{
name|newHeaders
operator|.
name|put
argument_list|(
name|header
operator|.
name|getKey
argument_list|()
argument_list|,
name|header
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|putHeaders
argument_list|(
name|newHeaders
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|ThreadContextStruct
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ThreadContextStruct
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"use the other write to"
argument_list|)
throw|;
block|}
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|defaultHeaders
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
decl_stmt|;
if|if
condition|(
name|defaultHeaders
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|headers
operator|=
name|this
operator|.
name|headers
expr_stmt|;
block|}
else|else
block|{
name|headers
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|defaultHeaders
argument_list|)
expr_stmt|;
name|headers
operator|.
name|putAll
argument_list|(
name|this
operator|.
name|headers
argument_list|)
expr_stmt|;
block|}
name|int
name|keys
init|=
name|headers
operator|.
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|keys
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|headers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|ContextThreadLocal
specifier|private
specifier|static
class|class
name|ContextThreadLocal
extends|extends
name|CloseableThreadLocal
argument_list|<
name|ThreadContextStruct
argument_list|>
block|{
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|set
specifier|public
name|void
name|set
parameter_list|(
name|ThreadContextStruct
name|object
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|object
operator|==
name|DEFAULT_CONTEXT
condition|)
block|{
name|super
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|set
argument_list|(
name|object
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|ex
parameter_list|)
block|{
comment|/* This is odd but CloseableThreadLocal throws a NPE if it was closed but still accessed.                    to get a real exception we call ensureOpen() to tell the user we are already closed.*/
name|ensureOpen
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|ThreadContextStruct
name|get
parameter_list|()
block|{
try|try
block|{
name|ThreadContextStruct
name|threadContextStruct
init|=
name|super
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|threadContextStruct
operator|!=
literal|null
condition|)
block|{
return|return
name|threadContextStruct
return|;
block|}
return|return
name|DEFAULT_CONTEXT
return|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|ex
parameter_list|)
block|{
comment|/* This is odd but CloseableThreadLocal throws a NPE if it was closed but still accessed.                    to get a real exception we call ensureOpen() to tell the user we are already closed.*/
name|ensureOpen
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
DECL|method|ensureOpen
specifier|private
name|void
name|ensureOpen
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"threadcontext is already closed"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit
