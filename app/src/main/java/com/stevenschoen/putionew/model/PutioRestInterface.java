package com.stevenschoen.putionew.model;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.stevenschoen.putionew.PutioUtils;
import com.stevenschoen.putionew.model.responses.AccountInfoResponse;
import com.stevenschoen.putionew.model.responses.BasePutioResponse;
import com.stevenschoen.putionew.model.responses.CachedFilesListResponse;
import com.stevenschoen.putionew.model.responses.FileResponse;
import com.stevenschoen.putionew.model.responses.FilesListResponse;
import com.stevenschoen.putionew.model.responses.FilesSearchResponse;
import com.stevenschoen.putionew.model.responses.Mp4StatusResponse;
import com.stevenschoen.putionew.model.responses.TransfersListResponse;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface PutioRestInterface {
	@GET("/files/list")
	FilesListResponse files(@Query("parent_id") int parentId);

	@GET("/files/search/{query}/page/-1")
	FilesSearchResponse search(@Path("query") String query);

	@GET("/files/{id}")
	FileResponse file(@Path("id") int id);

	@GET("/files/{id}/mp4")
	Mp4StatusResponse mp4Status(@Path("id") int id);

	@POST("/files/{id}/mp4")
	BasePutioResponse convertToMp4(@Path("id") int id);

	@GET("/transfers/list")
	TransfersListResponse transfers();

	@GET("/account/info")
	AccountInfoResponse account();

	public static abstract class PutioJob extends Job {
		private PutioUtils utils;

		protected PutioJob(Params params, PutioUtils utils) {
			super(params);
			this.utils = utils;
		}

		protected PutioUtils getUtils() {
			return utils;
		}
	}

	public static class GetFilesListJob extends PutioJob {
		private int parentId;

		private boolean alsoUseCache;

		public GetFilesListJob(PutioUtils utils, int parentId, boolean alsoUseCache) {
			super(new Params(0), utils);
			this.parentId = parentId;
			this.alsoUseCache = alsoUseCache;
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			if (alsoUseCache) {
				CachedFilesListResponse cachedResponse = getUtils().getFilesCache().getCached(parentId);
				if (cachedResponse != null) {
					getUtils().getEventBus().post(cachedResponse);
				}
			}

			FilesListResponse networkResponse = getUtils().getRestInterface().files(parentId);
			if (alsoUseCache) getUtils().getFilesCache().cache(networkResponse, parentId);
			getUtils().getEventBus().post(networkResponse);
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}

	public static class GetFilesSearchJob extends PutioJob {
		private String query;

		public GetFilesSearchJob(PutioUtils utils, String query) {
			super(new Params(0).requireNetwork(), utils);
			this.query = query;
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			FilesSearchResponse networkResponse = getUtils().getRestInterface().search(query);
			getUtils().getEventBus().post(networkResponse);
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}

	public static class GetFileJob extends PutioJob {
		private int id;

		public GetFileJob(PutioUtils utils, int id) {
			super(new Params(0), utils);
			this.id = id;
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			FileResponse networkResponse = getUtils().getRestInterface().file(id);
			getUtils().getEventBus().post(networkResponse);
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}

	public static class GetMp4StatusJob extends PutioJob {
		private int id;

		public GetMp4StatusJob(PutioUtils utils, int id) {
			super(new Params(0), utils);
			this.id = id;
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			Mp4StatusResponse networkResponse = getUtils().getRestInterface().mp4Status(id);
			getUtils().getEventBus().post(networkResponse);
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}

	public static class PostConvertToMp4Job extends PutioJob {
		private int id;

		public PostConvertToMp4Job(PutioUtils utils, int id) {
			super(new Params(0).requireNetwork(), utils);
			this.id = id;
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			getUtils().getRestInterface().convertToMp4(id);
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}

	public static class GetTransfersJob extends PutioJob {
		public GetTransfersJob(PutioUtils utils) {
			super(new Params(0).requireNetwork().groupBy("gettransfers"), utils);
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			getUtils().getEventBus().post(getUtils().getRestInterface().transfers());
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}

	public static class GetAccountInfoJob extends PutioJob {
		public GetAccountInfoJob(PutioUtils utils) {
			super(new Params(0).requireNetwork(), utils);
		}

		@Override
		public void onAdded() { }

		@Override
		public void onRun() throws Throwable {
			getUtils().getEventBus().post(getUtils().getRestInterface().account());
		}

		@Override
		protected void onCancel() { }

		@Override
		protected boolean shouldReRunOnThrowable(Throwable throwable) {
			return false;
		}
	}
}