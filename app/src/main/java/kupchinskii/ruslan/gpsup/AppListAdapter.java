package kupchinskii.ruslan.gpsup;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter   extends BaseAdapter {
    private List<ApplicationInfo> packages;
    private LayoutInflater inflater;
    private PackageManager pm;

    public AppListAdapter(Context context, PackageManager pm, List<ApplicationInfo> packages) {
        this.packages = packages;
        this.pm = pm;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return packages.size();
    }

    @Override
    public ApplicationInfo getItem(int p1) {

        return packages.get(p1);
    }

    @Override
    public long getItemId(int p1) {

        return p1;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        View view = null;
        ViewHolder viewHolder;

        if (v == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.app_list_item, parent, false);
            viewHolder.tvAppLabel = (TextView) view.findViewById(R.id.app_item_text);
            viewHolder.ivAppIcon = (ImageView) view.findViewById(R.id.app_item_image);
            view.setTag(viewHolder);
        } else {
            view = v;
            viewHolder = (ViewHolder) view.getTag();
        }

        ApplicationInfo app = packages.get(position);

        viewHolder.tvAppLabel.setText(app.loadLabel(pm).toString());
        viewHolder.ivAppIcon.setImageDrawable(app.loadIcon(pm));

        return view;
    }

    static class ViewHolder {
        private TextView tvAppLabel;
        private ImageView ivAppIcon;
    }

}
